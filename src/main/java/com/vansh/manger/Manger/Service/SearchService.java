package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.GlobalSearchResponseDTO;
import com.vansh.manger.Manger.DTO.SearchResultDTO;
import com.vansh.manger.Manger.Entity.AcademicYear; // --- NEW IMPORT ---
import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.Student;
import com.vansh.manger.Manger.Entity.Teacher;
import com.vansh.manger.Manger.Repository.*; // --- IMPORT ALL ---
import com.vansh.manger.Manger.Specification.SearchSpecification;
import com.vansh.manger.Manger.util.AdminSchoolConfig;

import jakarta.persistence.EntityNotFoundException; // --- NEW IMPORT ---
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // --- NEW IMPORT ---

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final StudentRepository studentRepository;
    private final TeacherRespository teacherRespository;
    private final ClassroomRespository classroomRespository;
    private final AdminSchoolConfig getCurrentSchool;

    // --- NEWLY REQUIRED REPOSITORIES ---
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicYearRepository academicYearRepository;

    @Transactional(readOnly = true) // Required for lazy-loading or new queries
    public GlobalSearchResponseDTO performGlobalSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new GlobalSearchResponseDTO();
        }

        PageRequest pageRequest = PageRequest.of(0, 5); // Limit all searches to 5 results

        // Student search logic (unchanged)
        Specification<Student> studentSpec = SearchSpecification.studentNameLike(query);
        List<SearchResultDTO> students = studentRepository.findAll(studentSpec, pageRequest).getContent()
                .stream()
                .map(s -> new SearchResultDTO(s.getId(), s.getFirstName() + " " + s.getLastName(), "Student", "/admin/students/" + s.getId()))
                .collect(Collectors.toList());

        // Teacher search logic (unchanged)
        Specification<Teacher> teacherSpec = SearchSpecification.teacherNameLike(query);
        List<SearchResultDTO> teachers = teacherRespository.findAll(teacherSpec, pageRequest).getContent()
                .stream()
                .map(t -> new SearchResultDTO(t.getId(), t.getFirstName() + " " + t.getLastName(), "Teacher", "/admin/teachers/" + t.getId()))
                .collect(Collectors.toList());

        // --- REFACTORED CLASSROOM SEARCH ---
        AcademicYear currentYear = academicYearRepository.findByIsCurrentAndSchool_Id(true, getCurrentSchool.requireCurrentSchool().getId()).orElse(null);
        Specification<Classroom> classroomSpec = SearchSpecification.classroomNameLike(query);
        List<SearchResultDTO> classrooms = classroomRespository.findAll(classroomSpec, pageRequest).getContent()
                .stream()
                .map(c -> {
                    // Get the current student count
                    long studentCount = (currentYear != null)
                            ? enrollmentRepository.countByClassroomAndAcademicYearAndSchool_Id(c, currentYear, getCurrentSchool.requireCurrentSchool().getId())
                            : 0;
                    return new SearchResultDTO(c.getId(), c.getName(), studentCount + " Students Enrolled", "Classroom", "/admin/classrooms");
                })
                .collect(Collectors.toList());

        GlobalSearchResponseDTO response = new GlobalSearchResponseDTO();
        response.setStudents(students);
        response.setTeachers(teachers);
        response.setClassrooms(classrooms);

        return response;
    }
}