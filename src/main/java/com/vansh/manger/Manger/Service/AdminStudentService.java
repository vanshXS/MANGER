package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.Config.RandomPasswordGenerator;
import com.vansh.manger.Manger.DTO.ClassroomResponseDTO;
import com.vansh.manger.Manger.DTO.StudentRequestDTO;
import com.vansh.manger.Manger.DTO.StudentResponseDTO;
import com.vansh.manger.Manger.DTO.SubjectResponseDTO;

import com.vansh.manger.Manger.Entity.AcademicYear;
import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.Enrollment;
import com.vansh.manger.Manger.Entity.StudentStatus;
import com.vansh.manger.Manger.Entity.Roles;
import com.vansh.manger.Manger.Entity.School;
import com.vansh.manger.Manger.Entity.Student;
import com.vansh.manger.Manger.Entity.StudentSubjectEnrollment;
import com.vansh.manger.Manger.Entity.Subject;
import com.vansh.manger.Manger.Entity.TeacherAssignment;
import com.vansh.manger.Manger.Entity.User;

import com.vansh.manger.Manger.Repository.AcademicYearRepository;
import com.vansh.manger.Manger.Repository.ClassroomRespository;
import com.vansh.manger.Manger.Repository.ClassroomSubjectRepository;
import com.vansh.manger.Manger.Repository.EnrollmentRepository;
import com.vansh.manger.Manger.Repository.StudentRepository;
import com.vansh.manger.Manger.Repository.StudentSubjectEnrollmentRepository;
import com.vansh.manger.Manger.Repository.StudentSubjectMarksRepository;
import com.vansh.manger.Manger.Repository.SubjectRepository;
import com.vansh.manger.Manger.Repository.TeacherAssignmentRepository;
import com.vansh.manger.Manger.Repository.UserRepo;
import com.vansh.manger.Manger.Specification.StudentSpecification;
import com.vansh.manger.Manger.util.AdminSchoolConfig;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStudentService {

    // --- All Repositories ---
    private final StudentRepository studentRepository;
    private final ClassroomRespository classroomRespository;
    private final SubjectRepository subjectRepository;
    private final StudentSubjectMarksRepository studentSubjectsRepository;
    private final AcademicYearRepository academicYearRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepo userRepo;
    private final AdminSchoolConfig getCurrentSchool;
    private final StudentSubjectEnrollmentRepository studentSubjectEnrollmentRepository;


    // --- Other Services & Components ---
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;

    private final RandomPasswordGenerator randomPasswordGenerator = new RandomPasswordGenerator();

    private static final String UPLOAD_DIR =
            System.getProperty("user.home") + "/manger/uploads/students/";
    private final ClassroomSubjectRepository classroomSubjectRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;


    /**
     * This is the "Admission" process.
     * It creates a Student, a User, and their first Enrollment record.
     */
    @Transactional
    public StudentResponseDTO createStudent(StudentRequestDTO studentRequestDTO) throws IOException {

        School school = getCurrentSchool.requireCurrentSchool();
        // 1. Validation
        if (studentRepository.existsByEmailAndSchool_Id(studentRequestDTO.getEmail(), school.getId())) {
            throw new IllegalArgumentException("Student with this email already registered");
        }


        Classroom classroom = classroomRespository.findByIdAndSchool(studentRequestDTO.getClassroomId(), school)
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found in this school: " + school.getName()));

        AcademicYear currentYear = academicYearRepository.findByIsCurrentAndSchool_Id(true, getCurrentSchool.requireCurrentSchool().getId())
                .orElseThrow(() -> new IllegalStateException("No active academic year is set! Cannot enroll student."));

        if(currentYear.getIsCurrent()){
            throw new IllegalStateException("Academic year is close. Please promote the student first.");
        }

        // 2. File Upload
        String pictureUrl = null;
        if (studentRequestDTO.getProfilePicture() != null && !studentRequestDTO.getProfilePicture().isEmpty()) {

                // Use email prefix for a unique filename, as rollNo isn't generated yet
               pictureUrl  = fileStorageService.saveStudentProfile(
                       studentRequestDTO.getProfilePicture(), studentRequestDTO.getEmail().split("@")[0]
               );

        }



        // 3. Password & User Creation
        String rawPassword = randomPasswordGenerator.generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User studentUser = User.builder()
                .fullName(studentRequestDTO.getFirstName() + " " + studentRequestDTO.getLastName())
                .email(studentRequestDTO.getEmail())
                .password(encodedPassword)
                .roles(Roles.STUDENT)
                .school(school)
                .build();


        //this fix the avatar not showing in frontend
        String imageUrl = pictureUrl != null
                ?  pictureUrl
                : null;

        // 4. Student Entity Creation
        Student student = Student.builder()
                .firstName(studentRequestDTO.getFirstName())
                .lastName(studentRequestDTO.getLastName())
                .user(studentUser)
                .school(school)
                .email(studentRequestDTO.getEmail())
                .password(encodedPassword)
                .phoneNumber(studentRequestDTO.getPhoneNumber())
                .profilePictureUrl(imageUrl).build();


        Student savedStudent = studentRepository.save(student);

        // 5. Auto-Generated Roll Number
        String newRollNo = generateNextRollNoForClass(classroom, currentYear);

        // 6. Create the Enrollment Record
        Enrollment firstEnrollment = Enrollment.builder()
                .student(savedStudent)
                .classroom(classroom)
                .academicYear(currentYear)
                .status(StudentStatus.ACTIVE)
                .rollNo(newRollNo)
                .school(school)
                .build();

        enrollmentRepository.save(firstEnrollment);

        // 7. Send Welcome Email
        try {
            emailService.sendNewUserWelcomeEmail(savedStudent.getEmail(), savedStudent.getFirstName(), rawPassword);
            activityLogService.logActivity(
                    "New student enrolled: " + savedStudent.getFirstName() + " (" + newRollNo + "). Welcome email sent.",
                    "Student Enrollment"
            );
        } catch (Exception e) {
            // Log the error but don't fail the transaction
            System.err.println("CRITICAL: Failed to send welcome email for student " + savedStudent.getId() + ": " + e.getMessage());
            activityLogService.logActivity(
                    "New student enrolled: " + savedStudent.getFirstName() + ". FAILED TO SEND EMAIL.",
                    "Student Enrollment (Error)"
            );
        }

        // 8. Map to Response DTO
        StudentResponseDTO responseDTO = mapToStudentResponseDTO(savedStudent, firstEnrollment);

        // --- SECURITY ---
        // Only set the raw password on this *one-time* response DTO for the admin
        responseDTO.setPassword(rawPassword);
        return responseDTO;
    }

    /**
     * Gets a paginated list of all students *and their current* enrollment details.
     */
    @Transactional
     public Page<StudentResponseDTO> getAllStudents(StudentStatus status, String search, Pageable pageable) {
        School school = getCurrentSchool.requireCurrentSchool();

        Specification<Student> spec = StudentSpecification.build(status, search, school.getId());

        Page<Student> page = studentRepository.findAll(spec, pageable);

        return page.map(student ->  {
            Enrollment currentEnrollment = enrollmentRepository.findByStudentAndSchool_IdAndAcademicYearIsCurrent(student,getCurrentSchool.requireCurrentSchool().getId(), true).orElse(null);
            return mapToStudentResponseDTO(student, currentEnrollment);
        });
    }

    /**
     * Gets a single student by their permanent ID, including their current enrollment.
     */
    @Transactional
    public StudentResponseDTO getStudentById(Long studentId) {

        School school = getCurrentSchool.requireCurrentSchool();

        Student student = studentRepository.findByIdAndSchool_Id(studentId, school.getId() )
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        Enrollment currentEnrollment = enrollmentRepository.findByStudentAndSchool_IdAndAcademicYearIsCurrent(student,getCurrentSchool.requireCurrentSchool().getId(), true)
                .orElse(null);

        return mapToStudentResponseDTO(student, currentEnrollment);
    }

    /**
     * Updates a student's *profile information* only.
     * This method does NOT change their classroom enrollment.
     */
    @Transactional
    public StudentResponseDTO updateStudent(Long studentId, StudentRequestDTO studentRequestDTO) throws IOException {
        School school = getCurrentSchool.requireCurrentSchool();

        Student existedStudent = studentRepository.findByIdAndSchool_Id(studentId, school.getId())
                .orElseThrow(() -> new EntityNotFoundException("Student not present in the existed school."));

        String newPic = existedStudent.getProfilePictureUrl();
        if (studentRequestDTO.getProfilePicture() != null && !studentRequestDTO.getProfilePicture().isEmpty()) {
            // Use a unique, stable identifier like email prefix or ID
            newPic = fileStorageService.saveStudentProfile(
                    studentRequestDTO.getProfilePicture(), existedStudent.getEmail().split("@")[0]
            );

        }

        // Check for email conflict
        studentRepository.findByEmailAndSchool_Id(studentRequestDTO.getEmail(), school.getId()).ifPresent(s -> {
            if (!s.getId().equals(studentId)) throw new IllegalArgumentException("This email is already taken.");
        });

        // Note: We don't update rollNo or classroomId here, as that's an enrollment change.

        existedStudent.setFirstName(studentRequestDTO.getFirstName());
        existedStudent.setLastName(studentRequestDTO.getLastName());
        existedStudent.setEmail(studentRequestDTO.getEmail());
        existedStudent.setPhoneNumber(studentRequestDTO.getPhoneNumber());

        existedStudent.setProfilePictureUrl(newPic);

        // keep associated User entity in sync
        existedStudent.getUser().setFullName(studentRequestDTO.getFirstName() + " " + existedStudent.getLastName());
        existedStudent.getUser().setEmail(studentRequestDTO.getEmail());

        Student updated = studentRepository.save(existedStudent);

        activityLogService.logActivity(
                "Updated student profile: " + updated.getFirstName() + " " + updated.getLastName(),
                "Student Update"
        );

        // Find their current enrollment to send back the full DTO
        Enrollment currentEnrollment = enrollmentRepository.findByStudentAndSchool_IdAndAcademicYearIsCurrent(updated, getCurrentSchool.requireCurrentSchool().getId(),true)
                .orElse(null);

        return mapToStudentResponseDTO(updated, currentEnrollment);
    }


    /**
     * --- RE-IMPLEMENTED AS A "TRANSFER" ---
     * Assigns (or transfers) a student to a different classroom *within the current academic year*.
     */
    @Transactional
    public StudentResponseDTO assignStudentToClassroom(Long studentId, Long newClassroomId) {
        School adminSchool = getCurrentSchool.requireCurrentSchool();

        Student student = studentRepository.findByIdAndSchool_Id(studentId, adminSchool.getId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        Classroom newClassroom = classroomRespository.findByIdAndSchool(newClassroomId, adminSchool)
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found"));
        AcademicYear currentYear = academicYearRepository.findByIsCurrentAndSchool_Id(true, getCurrentSchool.requireCurrentSchool().getId())
                .orElseThrow(() -> new IllegalStateException("No active academic year is set!"));

        if(student.getUser().getSchool() == null || newClassroom.getSchool() == null) {
            throw new IllegalStateException("School information is missing.");
        }
        if(!student.getUser().getSchool().getId().equals(newClassroom.getSchool().getId())){
            throw new IllegalArgumentException("Student and Classroom must be in the same school.");
        }



        // Find the student's *current* enrollment
        Optional<Enrollment> existingEnrollmentOpt = enrollmentRepository.findByStudentAndSchool_IdAndAcademicYearIsCurrent(student,getCurrentSchool.requireCurrentSchool().getId(), true);



        Enrollment enrollmentToSave;
        if (existingEnrollmentOpt.isPresent()) {
            // --- This is a TRANSFER ---
            Enrollment existingEnrollment = existingEnrollmentOpt.get();
            String oldClassroomName = existingEnrollment.getClassroom().getName();

               //new rollNO
            String newRollNo = generateNextRollNoForClass(newClassroom, currentYear);
            existingEnrollment.setClassroom(newClassroom);
            existingEnrollment.setRollNo(newRollNo);
            // Simply update the classroom
            enrollmentToSave = enrollmentRepository.save(existingEnrollment);
            autoAssignMandatorySubjects(student, newClassroom);
            activityLogService.logActivity(
                    "Student " + student.getFirstName() + " transferred from " + oldClassroomName + " to " + newClassroom.getName(),
                    "Student Transfer"
            );
        } else {
            // --- This is a NEW ENROLLMENT (for an unassigned student) ---
            String newRollNo = generateNextRollNoForClass(newClassroom, currentYear);
            Enrollment newEnrollment = Enrollment.builder()
                    .student(student)
                    .classroom(newClassroom)
                    .academicYear(currentYear)
                    .rollNo(newRollNo)
                    .build();
            enrollmentToSave = enrollmentRepository.save(newEnrollment);
            autoAssignMandatorySubjects(student, newClassroom);

            activityLogService.logActivity(
                    "Student " + student.getFirstName() + " enrolled in " + newClassroom.getName(),
                    "Student Enrollment"
            );
        }

        return mapToStudentResponseDTO(student, enrollmentToSave);
    }

    /**
     * --- NEW METHOD ---
     * Removes a student from their *current* classroom by deleting their
     * enrollment for the *active* academic year.
     */
    @Transactional
    public void removeStudentFromClassroom(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        Enrollment currentEnrollment = enrollmentRepository.findByStudentAndSchool_IdAndAcademicYearIsCurrent(student,getCurrentSchool.requireCurrentSchool().getId(), true)
                .orElseThrow(() -> new EntityNotFoundException("Student is not currently enrolled in any class."));

        String classroomName = currentEnrollment.getClassroom().getName();

        enrollmentRepository.delete(currentEnrollment);

        activityLogService.logActivity(
                "Student " + student.getFirstName() + " was unenrolled from " + classroomName,
                "Student Unenrollment"
        );
    }

    /**
     * Deletes a student and ALL their associated data (User, Enrollments, Subject links).
     * This is a permanent, destructive action.
     */
    @Transactional
    public void deleteById(Long studentId) {
        School school = getCurrentSchool.requireCurrentSchool();
        Student student = studentRepository.findByIdAndSchool_Id(studentId, school.getId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

         Enrollment enrollment = enrollmentRepository.findByStudentAndSchool_IdAndAcademicYearIsCurrent(student,getCurrentSchool.requireCurrentSchool().getId(), true)
                 .orElseThrow(() -> new RuntimeException("Student has no active enrollment."));

         if(enrollment.getStatus() != StudentStatus.INACTIVE) {
             throw new IllegalStateException("Student must be INACTIVE before permanent deletion");
         }



        // --- DATA INTEGRITY CASCADE ---
        // 1. Delete all subject assignments (electives)

        studentSubjectsRepository.deleteByStudentId(studentId);
        studentSubjectEnrollmentRepository.deleteByStudentId(studentId);

        // 2. Delete all enrollment history
        enrollmentRepository.deleteByStudentId(studentId);

        // 3. Delete the student
        studentRepository.delete(student);



        activityLogService.logActivity(
                "Deleted student: " + student.getFirstName() + " " + student.getLastName(),
                "Student Deletion"
        );
    }

    /**
     * Gets all students *currently* enrolled in a specific classroom for the *active* year.
     */
    @Transactional
    public List<StudentResponseDTO> getStudentsByClassroom(Long classroomId) {
        School school = getCurrentSchool.requireCurrentSchool();
        Classroom classroom = classroomRespository.findByIdAndSchool(classroomId, school)
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found"));

        AcademicYear currentYear = academicYearRepository.findByIsCurrentAndSchool_Id(true, getCurrentSchool.requireCurrentSchool().getId())
                .orElseThrow(() -> new IllegalStateException("No active academic year is set!"));

        List<Enrollment> enrollments = enrollmentRepository.findByClassroomAndSchool_IdAndAcademicYear(classroom,getCurrentSchool.requireCurrentSchool().getId(), currentYear);

        return enrollments.stream()
                .map(enrollment -> mapToStudentResponseDTO(enrollment.getStudent(), enrollment))
                .toList();
    }

    /**
     * Update Enrollment Status
     */

    @Transactional
      public void updateStatus(Long studentId, StudentStatus status) {

        School school = getCurrentSchool.requireCurrentSchool();

          Student student = studentRepository.findByIdAndSchool_Id(studentId, school.getId())
                  .orElseThrow(() -> new EntityNotFoundException("Student not found."));

           Enrollment enrollment = enrollmentRepository.findByStudentAndSchool_IdAndAcademicYearIsCurrent(student,getCurrentSchool.requireCurrentSchool().getId(), true)
                   .orElseThrow(() -> new EntityNotFoundException("Student has no active enrollment"));

           if(enrollment.getStatus() == status) return;

           enrollment.setStatus(status);
           enrollmentRepository.save(enrollment);

           activityLogService.logActivity(
                   "Student: " + student.getFirstName() + " status change to " + status,
                   "Student Status Update"
           );

      }


    /**
     * Assigns an elective subject to a student.
     */
    @Transactional
    public StudentResponseDTO assignStudentToSubject(Long studentId, Long subjectId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));

        Enrollment enrollment = enrollmentRepository
                .findByStudentAndSchool_IdAndAcademicYearIsCurrent(student,getCurrentSchool.requireCurrentSchool().getId(), true)
                .orElseThrow(() -> new IllegalStateException("Student not enrolled in any classroom"));

        Classroom classroom = enrollment.getClassroom();

        TeacherAssignment assignment = teacherAssignmentRepository
                .findByClassroomAndSubject(classroom, subject)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subject not part of classroom curriculum"
                ));

        if (assignment.isMandatory()) {
            throw new IllegalStateException(
                    "Mandatory subjects are auto-assigned and cannot be manually added"
            );
        }

        boolean exists = studentSubjectEnrollmentRepository
                .existsByStudentAndSubject(student, subject);

        if (exists) {
            throw new IllegalStateException("Subject already assigned to student");
        }

        StudentSubjectEnrollment enrollmentEntry =
                StudentSubjectEnrollment.builder()
                        .student(student)
                        .subject(subject)
                        .mandatory(false)
                        .build();

        studentSubjectEnrollmentRepository.save(enrollmentEntry);

        activityLogService.logActivity(
                "Optional subject " + subject.getName() +
                        " assigned to student " + student.getFirstName(),
                "Student Subject Assignment"
        );

        Enrollment currentEnrollment =
                enrollmentRepository.findByStudentAndSchool_IdAndAcademicYearIsCurrent(student, getCurrentSchool.requireCurrentSchool().getId(), true).orElse(null);

        return mapToStudentResponseDTO(student, currentEnrollment);
    }

    /**
     * Removes an elective subject from a student.
     */
    @Transactional
    public void removeSubjectFromStudent(Long studentId, Long subjectId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));

        // 1️⃣ Get student's active classroom via enrollment
        Enrollment enrollment = enrollmentRepository
                .findActiveByStudent(student)
                .orElseThrow(() ->
                        new IllegalStateException("Student is not enrolled in any class")
                );

        Classroom classroom = enrollment.getClassroom();

        // 2️⃣ Check if subject is mandatory for this classroom
        boolean isMandatoryForClass =
                teacherAssignmentRepository
                        .existsByClassroomAndSubjectAndMandatoryTrue(
                                classroom,
                                subject
                        );

        if (isMandatoryForClass) {
            throw new IllegalStateException(
                    "Mandatory subjects cannot be removed"
            );
        }

        // 3️⃣ Find student-subject enrollment
        StudentSubjectEnrollment studentSubjectEnrollment =
                studentSubjectEnrollmentRepository
                        .findByStudentAndSubject(student, subject)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Subject not assigned to student")
                        );

        // 4️⃣ Delete (only optional subjects reach here)
        studentSubjectEnrollmentRepository.delete(studentSubjectEnrollment);

        activityLogService.logActivity(
                "Optional subject " + subject.getName() +
                        " removed from student " + student.getFirstName(),
                "Student Subject Update"
        );
    }




    /**
     * Gets a list of elective subjects (as DTOs) for a specific student.
     */
    @Transactional
    public List<SubjectResponseDTO> getSubjectsOfStudent(Long studentId) {
        School school = getCurrentSchool.requireCurrentSchool();

        if (!studentRepository.existsById(studentId)) {
            throw new EntityNotFoundException("Student not found");
        }

        List<StudentSubjectEnrollment> enrollments =
                studentSubjectEnrollmentRepository.findByStudentId(studentId);

        List<SubjectResponseDTO> responseList = new java.util.ArrayList<>();

        for (StudentSubjectEnrollment enrollment : enrollments) {
            Subject subject = enrollment.getSubject();

            SubjectResponseDTO dto = SubjectResponseDTO.builder()
                    .id(subject.getId())
                    .name(subject.getName())
                    .code(subject.getCode())
                    .mandatory(enrollment.isMandatory())
                    .build();

            responseList.add(dto);
        }

        return responseList;
    }



    /**
     * This is the new, secure "Send Password Reset" function.
     * It generates a new password and emails it to the student.
     */
    @Transactional
    public void sendPasswordReset(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        User user = student.getUser();
        if (user == null) {
            throw new EntityNotFoundException("Associated user account not found for this student.");
        }

        String newRawPassword = randomPasswordGenerator.generateRandomPassword();
        String newEncodedPassword = passwordEncoder.encode(newRawPassword);

        // Update password on both User and Student entities
        user.setPassword(newEncodedPassword);
        student.setPassword(newEncodedPassword);

        // Save the updated entities
        userRepo.save(user);
        studentRepository.save(student);

        // Send the new password to the student's email
        try {
            emailService.sendNewUserWelcomeEmail( // We can reuse the welcome email template
                    student.getEmail(),
                    student.getFirstName(),
                    newRawPassword
            );
            activityLogService.logActivity(
                    "Admin triggered password reset for student: " + student.getFirstName() + " " + student.getLastName(),
                    "Security"
            );
        } catch (Exception e) {
            System.err.println("Failed to send password reset email for student " + student.getId() + ": " + e.getMessage());
            // Even if email fails, the password *is* changed. We must not fail the transaction.
            // But we should throw a different exception so the admin knows the email failed.
            throw new RuntimeException("Password was reset, but failed to send email. Please notify the student manually.");
        }
    }

    // =================================================================
    // --- HELPER METHODS ---
    // =================================================================

    /**
     * This is the new, standout feature for auto-generating roll numbers.
     * e.g., "G10A-2024-001"
     */
    public String generateNextRollNoForClass(Classroom classroom, AcademicYear year) {
        long count = enrollmentRepository.countByClassroomAndAcademicYearAndSchool_Id(classroom, year, getCurrentSchool.requireCurrentSchool().getId());
        String nextId = String.format("%03d", count + 1); // "001", "002", etc.

        // Create a simple abbreviation for the class name
        String classAbbreviation = classroom.getName().replaceAll("[^A-Z0-9-]", "").replaceAll("GRADE", "G"); // "Grade 10 - A" -> "G10-A"

        String yearAbbreviation = String.valueOf(year.getStartDate().getYear()); // "2024"

        return classAbbreviation + "-" + yearAbbreviation + "-" + nextId;
    }


    private StudentResponseDTO mapToStudentResponseDTO(Student student, Enrollment currentEnrollment) {

        StudentResponseDTO.StudentResponseDTOBuilder dtoBuilder = StudentResponseDTO.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phoneNumber(student.getPhoneNumber())
                .profilePictureUrl(student.getProfilePictureUrl());

        // ✅ ONLY access enrollment if it exists
        if (currentEnrollment != null) {
            dtoBuilder.status(currentEnrollment.getStatus());
            dtoBuilder.currentEnrollmentId(currentEnrollment.getId());
            dtoBuilder.rollNo(currentEnrollment.getRollNo());
            dtoBuilder.academicYearName(currentEnrollment.getAcademicYear().getName());

            dtoBuilder.classroomResponseDTO(
                    ClassroomResponseDTO.builder()
                            .id(currentEnrollment.getClassroom().getId())
                            .name(currentEnrollment.getClassroom().getName())
                            .capacity(currentEnrollment.getClassroom().getCapacity())
                            .status(currentEnrollment.getClassroom().getStatus())
                            .studentCount(
                                    enrollmentRepository.countByClassroomAndAcademicYearAndSchool_Id(
                                            currentEnrollment.getClassroom(),
                                            currentEnrollment.getAcademicYear(),
                                            getCurrentSchool.requireCurrentSchool().getId()
                                    )
                            )
                            .build()
            );
        } else {
            // optional fallback
            dtoBuilder.status(StudentStatus.INACTIVE); // or null if you prefer
        }

        // subjects (this part is fine)
        List<StudentSubjectEnrollment> enrollments =
                studentSubjectEnrollmentRepository.findByStudentId(student.getId());

        List<SubjectResponseDTO> subjectsDTOs = enrollments.stream()
                .map(ss -> SubjectResponseDTO.builder()
                        .id(ss.getSubject().getId())
                        .name(ss.getSubject().getName())
                        .code(ss.getSubject().getCode())
                        .mandatory(ss.isMandatory())
                        .build())
                .collect(Collectors.toList());

        dtoBuilder.subjectResponseDTOS(subjectsDTOs);

        return dtoBuilder.build();
    }


    //helper method
    public void autoAssignMandatorySubjects(Student student, Classroom classroom) {

        List<TeacherAssignment> mandatoryAssignments =
                teacherAssignmentRepository.findByClassroomAndMandatoryTrue(classroom);

        for (TeacherAssignment assignment : mandatoryAssignments) {

            boolean exists =
                    studentSubjectEnrollmentRepository
                            .existsByStudentAndSubject(student, assignment.getSubject());

            if (!exists) {
                StudentSubjectEnrollment enrollment =
                        StudentSubjectEnrollment.builder()
                                .student(student)
                                .subject(assignment.getSubject())
                                .mandatory(true)
                                .build();

                studentSubjectEnrollmentRepository.save(enrollment);
            }
        }
    }

}