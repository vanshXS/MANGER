package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.SubjectAssignmentDetailDTO;
import com.vansh.manger.Manger.DTO.SubjectRequestDTO;
import com.vansh.manger.Manger.DTO.SubjectResponseDTO;
import com.vansh.manger.Manger.Entity.*;
import com.vansh.manger.Manger.Repository.ClassroomRespository;
import com.vansh.manger.Manger.Repository.SubjectRepository;
import com.vansh.manger.Manger.Repository.TeacherAssignmentRepository;
import com.vansh.manger.Manger.Repository.TeacherRespository;
import com.vansh.manger.Manger.util.AdminSchoolConfig;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSubjectService {

    private final SubjectRepository subjectRepository;
    private final ClassroomRespository classroomRespository;
    private final TeacherRespository teacherRespository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final ActivityLogService activityLogService;
    private final AdminSchoolConfig adminSchoolConfig;

    //helper method to map entity to dto
    public SubjectResponseDTO mapToResponse(Subject subject) {
        long count = teacherAssignmentRepository.countBySubject(subject);

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findBySubject(subject);

        List<SubjectAssignmentDetailDTO> assignmentDetails = assignments.stream()
                .map(a -> {
                    String teacherName = (a.getTeacher() != null)
                            ? a.getTeacher().getFirstName() + " " + a.getTeacher().getLastName()
                            : "Unassigned";
                    String classroomName = (a.getClassroom() != null)
                            ? a.getClassroom().getName()
                            : "N/A";
                    return new SubjectAssignmentDetailDTO(classroomName, teacherName);
                }).toList();

        return SubjectResponseDTO.builder()
                .id(subject.getId())
                .name(subject.getName())
                .code(subject.getCode())
                .assignmentCount(count)
                .subjectAssignmentDetailDTOS(assignmentDetails)
                .build();
    }

    @Transactional
      public SubjectResponseDTO createSubject(SubjectRequestDTO dto) {



        if(subjectRepository.existsByNameIgnoreCaseAndSchool_Id(dto.getName(), adminSchoolConfig.requireCurrentSchool().getId())) {
            throw new IllegalArgumentException("Subject name already exists: " + dto.getName());
        }
        if(subjectRepository.existsByCodeIgnoreCaseAndSchool_Id(dto.getCode(), adminSchoolConfig.requireCurrentSchool().getId())) {
            throw new IllegalArgumentException("Subject code already exists: " + dto.getCode());
        }

        Subject subject = Subject.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .school(adminSchoolConfig.requireCurrentSchool())
                .build();


        Subject savedSubject = subjectRepository.save(subject);
        activityLogService.logActivity("Created new subject: " + savedSubject.getName(), "Subject Management");
        //map to response dto
        return mapToResponse(savedSubject);
    }

      public List<SubjectResponseDTO> getAllSubjects() {
        return subjectRepository.findBySchool_Id(adminSchoolConfig.requireCurrentSchool().getId())
                .stream()
                .map(this :: mapToResponse)
                .collect(Collectors.toList());
      }


    @Transactional
    public void deleteSubject(Long subjectId) {


        Subject subject = subjectRepository.findByIdAndSchool_Id(subjectId, adminSchoolConfig.requireCurrentSchool().getId())
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with id in the school: " + subjectId));

        // --- BUSINESS LOGIC CHECK ---
        if (teacherAssignmentRepository.existsBySubject(subject)) {
            throw new IllegalStateException("Cannot delete subject: It is currently assigned to one or more classrooms/teachers. Please remove assignments first.");
        }

        subjectRepository.delete(subject);
        activityLogService.logActivity("Deleted subject: " + subject.getName(), "Subject Management");
    }


    @Transactional
    public SubjectResponseDTO updateSubject(Long subjectId, SubjectRequestDTO dto) {



        Subject existingSubject = subjectRepository.findByIdAndSchool_Id(subjectId, adminSchoolConfig.requireCurrentSchool().getId())
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with id: " + subjectId));

        // Validate potential name/code conflicts ONLY if they have changed
        if (!existingSubject.getName().equalsIgnoreCase(dto.getName()) && subjectRepository.existsByNameIgnoreCaseAndSchool_Id(dto.getName(), adminSchoolConfig.requireCurrentSchool().getId())) {
            throw new IllegalArgumentException("Another subject with this name already exists: " + dto.getName());
        }
        if (!existingSubject.getCode().equalsIgnoreCase(dto.getCode()) && subjectRepository.existsByCodeIgnoreCaseAndSchool_Id(dto.getCode(), adminSchoolConfig.requireCurrentSchool().getId())) {
            throw new IllegalArgumentException("Another subject with this code already exists: " + dto.getCode());
        }

        // Update only the allowed fields
        existingSubject.setName(dto.getName());
        existingSubject.setCode(dto.getCode().toUpperCase());


        Subject updatedSubject = subjectRepository.save(existingSubject);
        activityLogService.logActivity("Updated subject: " + updatedSubject.getName(), "Subject Management");
        return mapToResponse(updatedSubject); // Return DTO with updated count
    }

  @Transactional
    public List<SubjectResponseDTO> subjectsByClassroomId(Long classroomId) {

        return teacherAssignmentRepository.findByClassroomId(classroomId)
                .stream()
                .map(assignment -> {
                    Subject subject = assignment.getSubject();

                    return new SubjectResponseDTO(subject.getId(), subject.getName(), subject.getCode());
                }).toList();
  }


}


