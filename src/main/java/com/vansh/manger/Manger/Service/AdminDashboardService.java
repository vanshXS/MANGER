package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.ActivityLogDTO;
import com.vansh.manger.Manger.DTO.ClassroomEnrollmentDTO;
import com.vansh.manger.Manger.DTO.DashboardKpiDTO;
import com.vansh.manger.Manger.DTO.TeacherWorkloadDTO;
import com.vansh.manger.Manger.Entity.*;
import com.vansh.manger.Manger.Repository.*;
import com.vansh.manger.Manger.util.AdminSchoolConfig;

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
    private final AdminSchoolConfig getCurrentSchool;

    @Transactional
    public DashboardKpiDTO getKpis() {

        long totalStudents = studentRepository.count();
        long activeTeachers = teacherRespository.count();
        long unassignedTeacher = teacherRespository.findUnassignedTeachersBySchool_Id(getCurrentSchool.requireCurrentSchool().getId()).size();


        List<Classroom> classrooms = classroomRespository.findBySchoolAndStatus(getCurrentSchool.requireCurrentSchool(), ClassroomStatus.ACTIVE);
        int totalCapacity = classrooms.stream().mapToInt(Classroom::getCapacity).sum();

        AcademicYear currentYear = academicYearRepository.findByIsCurrentAndSchool_Id(true, getCurrentSchool.requireCurrentSchool().getId())
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
        Optional<AcademicYear> optionalYear = academicYearRepository.findByIsCurrentAndSchool_Id(true, getCurrentSchool.requireCurrentSchool().getId());

        if (optionalYear.isEmpty()) {
            // ✅ Return empty list safely
            return Collections.emptyList();
        }

        AcademicYear currentYear = optionalYear.get();

        return classroomRespository.findBySchool_Id(getCurrentSchool.requireCurrentSchool().getId()).stream()
                .map(classroom -> {
                    long studentCount = enrollmentRepository.countByClassroomAndAcademicYearAndSchool_Id(classroom, currentYear,getCurrentSchool.requireCurrentSchool().getId());
                    return new ClassroomEnrollmentDTO(
                            classroom.getName(),
                            classroom.getCapacity(),
                            (int) studentCount
                    );
                })
                .collect(Collectors.toList());
    }

    public List<TeacherWorkloadDTO> getTeacherWorkload() {
        return teacherRespository.findBySchool_Id(getCurrentSchool.requireCurrentSchool().getId())
                .stream()
                .map(teacher -> new TeacherWorkloadDTO(
                        teacher.getFirstName() + " " + teacher.getLastName(),
                        teacherAssignmentRepository.countByTeacher(teacher)
                ))
                .collect(Collectors.toList());
    }

    public List<ActivityLogDTO> getRecentActivity() {
        return activityLogRepository.findTop10BySchool_IdOrderByCreatedAtDesc(getCurrentSchool.requireCurrentSchool().getId())
                .stream()
                .map(this :: mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<ActivityLogDTO> getAllActivityLogs(Pageable pageable) {
        return activityLogRepository
                .findBySchool_IdOrderByCreatedAtDesc(getCurrentSchool.requireCurrentSchool().getId(), pageable)
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
