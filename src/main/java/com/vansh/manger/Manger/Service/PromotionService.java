package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.ClassroomResponseDTO;
import com.vansh.manger.Manger.DTO.PromotionContextDTO;
import com.vansh.manger.Manger.DTO.PromotionRequestDTO;
import com.vansh.manger.Manger.DTO.StudentPromotionDTO;
import com.vansh.manger.Manger.Entity.*;
import com.vansh.manger.Manger.Repository.*;
import com.vansh.manger.Manger.util.AdminSchoolConfig;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRespository classroomRespository;
    private final AcademicYearRepository academicYearRepository;
    private final ActivityLogService activityLogService;
    private final AdminSchoolConfig schoolConfig;
    private final AdminStudentService adminStudentService;
    private final StudentRepository studentRepository;

    /**
     * Returns real-time promotion context so the frontend can show readiness and guide the admin.
     */
    public PromotionContextDTO getPromotionContext() {
        School school = schoolConfig.requireCurrentSchool();
        Optional<AcademicYear> currentOpt = academicYearRepository.findByIsCurrentAndSchool_Id(true, school.getId());
        Optional<AcademicYear> closedOpt = academicYearRepository.findTopByClosedTrueAndSchool_IdOrderByEndDateDesc(school.getId());

        PromotionContextDTO.AcademicYearSummary currentSummary = currentOpt
                .map(y -> PromotionContextDTO.AcademicYearSummary.builder().id(y.getId()).name(y.getName()).build())
                .orElse(null);
        PromotionContextDTO.AcademicYearSummary closedSummary = closedOpt
                .map(y -> PromotionContextDTO.AcademicYearSummary.builder().id(y.getId()).name(y.getName()).build())
                .orElse(null);

        boolean canPromote = currentOpt.isPresent() && closedOpt.isPresent();
        String message = null;
        if (!canPromote) {
            if (closedOpt.isEmpty()) {
                message = "No closed academic year. Close the current year in Settings → Academic years first.";
            } else {
                message = "No active academic year. Set the new year as current in Settings → Academic years.";
            }
        }

        return PromotionContextDTO.builder()
                .canPromote(canPromote)
                .currentAcademicYear(currentSummary)
                .closedAcademicYear(closedSummary)
                .message(message)
                .build();
    }

    @Transactional
    public void promoteClassroom(PromotionRequestDTO dto) {

        School school = schoolConfig.requireCurrentSchool();

        // 1. Get the most recently CLOSED academic year (old year)
        AcademicYear oldYear = academicYearRepository.findTopByClosedTrueAndSchool_IdOrderByEndDateDesc(school.getId())
                .orElseThrow(() -> new IllegalStateException("No closed academic year found. Close the current year first."));
        //2. Get NEW current academic year

        AcademicYear newYear = academicYearRepository.findByIsCurrentAndSchool_Id(true, school.getId())
                .orElseThrow(() -> new IllegalStateException("No active academic year found."));


        if (!newYear.getStartDate().isAfter(oldYear.getStartDate())) {
            throw new IllegalArgumentException(
                    String.format("Cannot promote backwards. Target year (%s) starts before source year (%s).",
                            newYear.getName(), oldYear.getName())
            );
        }

        if (dto.getFromClassroomId().equals(dto.getToClassroomId())) {
            throw new IllegalArgumentException("Source and target classroom cannot be the same.");
        }
        Classroom oldClassroom = classroomRespository.findByIdAndSchool(dto.getFromClassroomId(), school)
                .orElseThrow(() -> new EntityNotFoundException("Source classroom not found"));
        Classroom newClassroom = classroomRespository.findByIdAndSchool(dto.getToClassroomId(), school)
                .orElseThrow(() -> new EntityNotFoundException("Target classroom not found"));

        List<Enrollment> enrollments =
                enrollmentRepository.findByClassroomAndAcademicYearAndStatus(oldClassroom, oldYear, StudentStatus.ACTIVE);

        if (enrollments.isEmpty()) {
            throw new IllegalStateException(
                "The selected source classroom (\"" + oldClassroom.getName() + "\") has no active students in the closed academic year (\"" + oldYear.getName() + "\"). " +
                "Add students to that class for that year, or choose a different source classroom."
            );
        }

                int promotedCount = 0;
                for (Enrollment oldEnrollment : enrollments) {
                    Student oldStudent = oldEnrollment.getStudent();

                    // Skip if student already has an enrollment in the new year (e.g. already promoted)
                    if (enrollmentRepository.existsByStudentAndAcademicYear(oldStudent, newYear)) continue;

                    String newRollNo = adminStudentService.generateNextRollNoForClass(newClassroom, newYear);

                    Enrollment newEnrollment =
                            Enrollment.builder()
                                    .student(oldStudent)
                                    .classroom(newClassroom)
                                    .rollNo(newRollNo)
                                    .academicYear(newYear)
                                    .status(StudentStatus.ACTIVE)
                                    .school(school)
                                    .build();

                    enrollmentRepository.save(newEnrollment);

                    oldEnrollment.setStatus(StudentStatus.PROMOTED);
                    enrollmentRepository.save(oldEnrollment);

                    adminStudentService.autoAssignMandatorySubjects(oldStudent, newClassroom);
                    promotedCount++;
                }
                activityLogService.logActivity(
                        "Promoted classroom " + oldClassroom.getName() + " to " + newClassroom.getName() + " (" + promotedCount + " students)",
                        "Promotion"
                );
    }

    @Transactional
    public void promoteSingleStudent(StudentPromotionDTO dto) {

        School school = schoolConfig.requireCurrentSchool();

        Student student = studentRepository
                .findByIdAndSchool_Id(dto.getStudentId(), school.getId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        AcademicYear oldYear =
                academicYearRepository.findTopByClosedTrueAndSchool_IdOrderByEndDateDesc(school.getId())
                        .orElseThrow(() -> new IllegalStateException("No closed academic year found."));

        AcademicYear newYear =
                academicYearRepository.findByIsCurrentAndSchool_Id(true, school.getId())
                        .orElseThrow(() -> new IllegalStateException("No active academic year"));

        Enrollment oldEnrollment =
                enrollmentRepository.findByStudentAndAcademicYear(student, oldYear)
                        .orElseThrow(() -> new IllegalStateException("Student not enrolled in old year"));

        switch (dto.getType()) {

            case PROMOTE, REPEAT -> {
                if (dto.getTargetClassroomId() == null) {
                    throw new IllegalArgumentException("Target classroom is required for PROMOTE or REPEAT.");
                }
                Classroom targetClass = classroomRespository
                        .findByIdAndSchool(dto.getTargetClassroomId(), school)
                        .orElseThrow(() -> new EntityNotFoundException("Target classroom not found"));

                String newRoll =
                        adminStudentService.generateNextRollNoForClass(targetClass, newYear);

                Enrollment newEnrollment = Enrollment.builder()
                        .student(student)
                        .classroom(targetClass)
                        .academicYear(newYear)
                        .rollNo(newRoll)
                        .status(StudentStatus.ACTIVE)
                        .school(school)
                        .build();

                enrollmentRepository.save(newEnrollment);

                oldEnrollment.setStatus(
                        dto.getType() == PromotionType.PROMOTE
                                ? StudentStatus.PROMOTED
                                : StudentStatus.REPEATED
                );

                enrollmentRepository.save(oldEnrollment);

                adminStudentService.autoAssignMandatorySubjects(student, targetClass);
            }

            case DROPOUT -> {
                oldEnrollment.setStatus(StudentStatus.DROPPED_OUT);
                enrollmentRepository.save(oldEnrollment);
            }

            case GRADUATE -> {
                oldEnrollment.setStatus(StudentStatus.GRADUATED);
                enrollmentRepository.save(oldEnrollment);
            }
        }

        activityLogService.logActivity(
                "Student " + student.getFirstName() +
                        " processed as " + dto.getType(),
                "Student Promotion"
        );
    }

    // NEW: Helper method for frontend
    public List<ClassroomResponseDTO> getClassroomsForAcademicYear(Long academicYearId) {
        School school = schoolConfig.requireCurrentSchool();

        return classroomRespository.findClassroomsWithEnrollments(academicYearId, school.getId())
                .stream()
                .map(c -> ClassroomResponseDTO.builder() // Assuming you have a mapper or DTO builder
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();
    }






}
