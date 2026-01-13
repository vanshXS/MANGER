package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.Config.RandomPasswordGenerator;
import com.vansh.manger.Manger.DTO.AssignmentRequestDTO;
import com.vansh.manger.Manger.DTO.TeacherAssignmentDTO;
import com.vansh.manger.Manger.DTO.TeacherRequestDTO;
import com.vansh.manger.Manger.DTO.TeacherResponseDTO;
import com.vansh.manger.Manger.Entity.*;
import com.vansh.manger.Manger.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class AdminTeacherService {

    private final TeacherRespository teacherRespository;
    private final ClassroomRespository classroomRespository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SubjectRepository subjectRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRepo userRepo;

    // ✅ FIX: Proper injection
    private final ActivityLogService activityLogService;

    private final RandomPasswordGenerator generator = new RandomPasswordGenerator();
    private final String UPLOAD_DIR =
            System.getProperty("user.home") + "/manger/uploads/teachers";

    // ---------------- PROFILE PICTURE ----------------
    public String saveProfilePicture(MultipartFile file, String email) {
        if (file == null || file.isEmpty()) return null;

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFileName =
                    email + "-" + System.currentTimeMillis() + extension;

            Path filePath = uploadPath.resolve(uniqueFileName);
            file.transferTo(filePath.toFile());

            return  uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile picture: " + e.getMessage(), e);
        }
    }

    // ---------------- CREATE TEACHER ----------------
    @Transactional
    public TeacherResponseDTO createTeacher(TeacherRequestDTO dto) {

        if (teacherRespository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("Teacher already exists with this email");




        String pictureUrl = saveProfilePicture(dto.getProfilePicture(), dto.getEmail());
        String rawPassword = generator.generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);



        String imageUrl = pictureUrl;

        User adminUser = findCurrentUser();

        User teacherUser = User.builder()
                .fullName(dto.getFirstName() + " " + dto.getLastName())
                .email(dto.getEmail())
                .password(encodedPassword)
                .roles(Roles.TEACHER)
                .school(adminUser.getSchool())
                .build();

        Teacher teacher = Teacher.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone_number(dto.getPhoneNumber())
                .password(encodedPassword)
                .email(dto.getEmail())
                .role(Roles.TEACHER)
                .profilePictureUrl(imageUrl)
                .user(teacherUser)
                .build();



        Teacher savedTeacher = teacherRespository.save(teacher);

        // ✅ ACTIVITY LOG
        activityLogService.logActivity(
                "Teacher created: " + savedTeacher.getFirstName() + " " + savedTeacher.getLastName(),
                "Teacher Management"
        );

        try {
            emailService.sendNewUserWelcomeEmail(
                    savedTeacher.getEmail(),
                    savedTeacher.getFirstName(),
                    rawPassword
            );
        } catch (Exception e) {
            activityLogService.logActivity(
                    "Failed to send welcome email to " + savedTeacher.getEmail(),
                    "Teacher Email Error"
            );
        }

        return mapToResponseWithAssignments(teacher);
    }

    // ---------------- UPDATE TEACHER ----------------
    @Transactional
    public TeacherResponseDTO updateTeacher(Long teacherId, TeacherRequestDTO dto) {

        Teacher teacher = teacherRespository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        String newPic = saveProfilePicture(dto.getProfilePicture(), dto.getEmail());

        teacher.setFirstName(dto.getFirstName());
        teacher.setLastName(dto.getLastName());
        teacher.setEmail(dto.getEmail());
        teacher.setPhone_number(dto.getPhoneNumber());

        if (newPic != null) {
            teacher.setProfilePictureUrl(newPic);
        }

        teacher.getUser().setFullName(dto.getFirstName() + " " + dto.getLastName());
        teacher.getUser().setEmail(dto.getEmail());

        teacherRespository.save(teacher);

        // ✅ ACTIVITY LOG
        activityLogService.logActivity(
                "Teacher updated: " + teacher.getFirstName() + " " + teacher.getLastName(),
                "Teacher Management"
        );

        return mapToResponseWithAssignments(teacher);
    }

    // ---------------- DELETE TEACHER ----------------
    @Transactional
    public void delete(Long teacherId) {

        Teacher teacher = teacherRespository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        teacherAssignmentRepository.deleteByTeacher(teacher);

        teacherRespository.delete(teacher);


        activityLogService.logActivity(
                "Teacher deleted: " + teacher.getFirstName() + " " + teacher.getLastName(),
                "Teacher Management"
        );
    }

    // ---------------- FETCH ----------------
    public List<TeacherResponseDTO> getAllTeachers() {
        return teacherRespository.findAll()
                .stream()
                .map(this::mapToResponseWithAssignments)
                .collect(Collectors.toList());
    }

    public TeacherResponseDTO getTeacherById(Long teacherId) {
        Teacher teacher = teacherRespository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found."));
        return mapToResponseWithAssignments(teacher);
    }

    // ---------------- MAPPER ----------------
    private TeacherResponseDTO mapToResponseWithAssignments(Teacher teacher) {

        List<TeacherAssignmentDTO> assignments =
                teacherAssignmentRepository.findByTeacher(teacher)
                        .stream()
                        .map(a -> TeacherAssignmentDTO.builder()
                                .assignmentId(a.getId())
                                .classroomId(a.getClassroom().getId())
                                .className(a.getClassroom().getName())
                                .subjectId(a.getSubject().getId())
                                .subjectName(a.getSubject().getName())
                                .mandatory(a.isMandatory())
                                .build()
                        )
                        .collect(Collectors.toList());

        return TeacherResponseDTO.builder()
                .id(teacher.getId())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .phoneNumber(teacher.getPhone_number())
                .email(teacher.getEmail())
                .profilePictureUrl(teacher.getProfilePictureUrl())
                .active(teacher.isActive())
                .joinDate(teacher.getJoiningDate() != null ? teacher.getJoiningDate().toString() : null)
                .assignedClassrooms(assignments)
                .build();


    }

    //------------------------------helper method -----------------------------

    private User findCurrentUser() {
        return userRepo.findByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new IllegalStateException("Admin not found"));
    }
}
