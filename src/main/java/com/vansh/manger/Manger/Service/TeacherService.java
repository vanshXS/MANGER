package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.ClassroomResponseDTO;
import com.vansh.manger.Manger.DTO.SubjectResponseDTO;
import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.Teacher;
import com.vansh.manger.Manger.Entity.TeacherAssignment;
import com.vansh.manger.Manger.Repository.TeacherAssignmentRepository;
import com.vansh.manger.Manger.Repository.TeacherRespository;
import com.vansh.manger.Manger.util.AdminSchoolConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherRespository teacherRespository;
    private final AdminSchoolConfig adminSchoolConfig;

    //get my classes

    public List<ClassroomResponseDTO> getMyClasses(String email) {

        Teacher teacher = teacherRespository.findByEmailAndSchool_Id(email, adminSchoolConfig.requireCurrentSchool().getId()).
                orElseThrow(() -> new RuntimeException("Teacher not found"));


        return teacherAssignmentRepository.findByTeacher(teacher)
                .stream().filter(Objects::nonNull)
                .map(ta -> {
                    ClassroomResponseDTO dto = new ClassroomResponseDTO();
                    dto.setId(ta.getClassroom().getId());
                    dto.setName(ta.getClassroom().getName());
                    return dto;
                }).toList();
    }

    public List<SubjectResponseDTO> getMySubject(String email) {
        Teacher teacher = teacherRespository.findByEmailAndSchool_Id(email, adminSchoolConfig.requireCurrentSchool().getId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return teacherAssignmentRepository.findByTeacher(teacher)
                .stream().filter(Objects :: nonNull)
                .map(ta -> {
                    SubjectResponseDTO dto = new SubjectResponseDTO();
                    dto.setId(ta.getSubject().getId());
                    dto.setName(ta.getSubject().getName());
                    return dto;
                }).toList();
    }


}
