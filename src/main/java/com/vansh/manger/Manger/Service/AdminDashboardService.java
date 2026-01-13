package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.ActivityLogDTO;
import com.vansh.manger.Manger.DTO.ClassroomEnrollmentDTO;
import com.vansh.manger.Manger.DTO.DashboardKpiDTO;
import com.vansh.manger.Manger.DTO.TeacherWorkloadDTO;
import com.vansh.manger.Manger.Entity.*;
import com.vansh.manger.Manger.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final StudentRepository studentRepository;
    private final TeacherRespository teacherRespository;
    private final ClassroomRespository classroomRespository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AdminServiceHelper adminServiceHelper;

    @Transactional
    public DashboardKpiDTO getKpis() {
        School adminSchool = adminServiceHelper.getCurrentAdminSchool();


        long totalStudents = studentRepository.count();
        long activeTeachers = teacherRespository.count();
        long unassignedTeacher = teacherRespository.findUnassignedTeachers().size();


        List<Classroom> classrooms = classroomRespository.findBySchoolAndStatus(adminSchool, ClassroomStatus.ACTIVE);
        int totalCapacity = classrooms.stream().mapToInt(Classroom::getCapacity).sum();

        AcademicYear currentYear = academicYearRepository.findByIsCurrent(true)
                .orElseThrow(() -> new IllegalStateException("No active academic year found!"));


        long totalEnrolledStudents = enrollmentRepository.countByAcademicYear(currentYear);

        int utilization = (totalCapacity > 0) ? (int) (((double) totalEnrolledStudents / totalCapacity) * 100) : 0;

        return DashboardKpiDTO.builder()
                .totalStudents(totalStudents)
                .activeTeachers(activeTeachers)
                .classroomUtilization(utilization)
                .unassignedTeachers(unassignedTeacher)
                .build();
    }

    public List<ClassroomEnrollmentDTO> getEnrollmentOverview() {
        Optional<AcademicYear> optionalYear = academicYearRepository.findByIsCurrent(true);

        if (optionalYear.isEmpty()) {
            // ✅ Return empty list safely
            return Collections.emptyList();
        }

        AcademicYear currentYear = optionalYear.get();

        return classroomRespository.findAll().stream()
                .map(classroom -> {
                    long studentCount = enrollmentRepository.countByClassroomAndAcademicYear(classroom, currentYear);
                    return new ClassroomEnrollmentDTO(
                            classroom.getName(),
                            classroom.getCapacity(),
                            (int) studentCount
                    );
                })
                .collect(Collectors.toList());
    }

    public List<TeacherWorkloadDTO> getTeacherWorkload() {
        return teacherRespository.findAll()
                .stream()
                .map(teacher -> new TeacherWorkloadDTO(
                        teacher.getFirstName() + " " + teacher.getLastName(),
                        teacherAssignmentRepository.countByTeacher(teacher)
                ))
                .collect(Collectors.toList());
    }

    public List<ActivityLogDTO> getRecentActivity() {
        return activityLogRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(this :: mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<ActivityLogDTO> getAllActivityLogs(Pageable pageable) {
        return activityLogRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToDTO);
    }




    //-----------------------helper function -------------------------

    private ActivityLogDTO mapToDTO(ActivityLog activityLog) {

        return ActivityLogDTO.builder()
                .description(activityLog.getDescription())
                .category(activityLog.getCategory())
                .date(activityLog.getCreatedAt())
                .build();
    }



}
