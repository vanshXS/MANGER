package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.ClassroomRequestDTO;
import com.vansh.manger.Manger.DTO.ClassroomResponseDTO;
import com.vansh.manger.Manger.DTO.StudentResponseDTO;
import com.vansh.manger.Manger.Entity.*;
import com.vansh.manger.Manger.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminClassroomService {

    private final ClassroomRespository classroomRespository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
    private final AcademicYearRepository academicYearRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepo userRepo;
    private final AdminServiceHelper adminServiceHelper;


   public ClassroomResponseDTO mapToResponse(Classroom classroom) {

       AcademicYear currentYear = academicYearRepository.findByIsCurrent(true)
               .orElse(null);
       long studentCount = 0;

       if(currentYear!= null) {
           studentCount = enrollmentRepository.countByClassroomAndAcademicYear(classroom, currentYear);
       }

       return ClassroomResponseDTO.builder()
               .id(classroom.getId())
               .name(classroom.getName())
               .capacity(classroom.getCapacity())
               .studentCount(studentCount)
               .status(classroom.getStatus())
               .build();
   }

   @Transactional
       public ClassroomResponseDTO createClassroom(ClassroomRequestDTO classroomRequestDTO) {

        School adminSchool = adminServiceHelper.getCurrentAdminSchool();

         if(classroomRespository.existsByNameAndSchool(classroomRequestDTO.getName(), adminSchool)) {
             throw new IllegalArgumentException("Classroom with this name already exists");
         }



         Classroom classroom = Classroom.builder()
                 .name(classroomRequestDTO.getName())
                 .status(ClassroomStatus.ACTIVE)
                 .capacity(classroomRequestDTO.getCapacity())
                 .school(adminSchool)
                 .build();

        Classroom savedClassroom =  classroomRespository.save(classroom);

         return mapToResponse(savedClassroom);
       }

       @Transactional
       public ClassroomResponseDTO updateClassroom(Long id, ClassroomRequestDTO classroomRequestDTO) {

       School adminSchool = adminServiceHelper.getCurrentAdminSchool();
           Classroom classroom = classroomRespository.findById(id)
                   .orElseThrow(() -> new EntityNotFoundException("Classroom not found"));
           // Optional: Check if the new name conflicts with another existing classroom
            classroomRespository.findByNameAndSchool(classroomRequestDTO.getName(), adminSchool).ifPresent(existing -> {
                if(!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Another classroom with this name already exists.");
                }
            });
           classroom.setName(classroomRequestDTO.getName());

           classroom.setCapacity(classroomRequestDTO.getCapacity());

           Classroom updatedClassroom = classroomRespository.save(classroom);

           return mapToResponse(updatedClassroom);
       }

       @Transactional
       public void deleteClassroom(Long id) {
         Classroom classroom = classroomRespository.findById(id)
                 .orElseThrow(() -> new EntityNotFoundException("Classroom not founded"));

           if (enrollmentRepository.existsByClassroom(classroom)) { // <-- Needs new repo method
               throw new IllegalStateException("Cannot delete classroom with enrollment history. Please archive it instead.");
           }
           // Checks if any teacher is/was assigned
           if (teacherAssignmentRepository.existsByClassroom(classroom)) { // <-- Needs new repo method
               throw new IllegalStateException("Cannot delete classroom with assigned teachers/subjects. Please archive it instead.");
           }

         classroomRespository.delete(classroom);
       }

       public List<ClassroomResponseDTO> getAllActiveClassrooms() {
       return classroomRespository.findBySchoolAndStatus(adminServiceHelper.getCurrentAdminSchool(), ClassroomStatus.ACTIVE)
               .stream()
               .map(this::mapToResponse)
               .collect(Collectors.toList());
       }

       public List<ClassroomResponseDTO> getClassroomsByStatus(ClassroomStatus status) {
                return classroomRespository.findBySchoolAndStatus(adminServiceHelper.getCurrentAdminSchool(), status)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();
       }

       // update status(for Archive/Activate)
       @Transactional
       public ClassroomResponseDTO updateClassroomStatus(Long id, ClassroomStatus newStatus) {
           Classroom classroom = classroomRespository.findById(id)
                   .orElseThrow(() -> new EntityNotFoundException("Classroom not found with this id: " + id));

           // --- FIXED BUSINESS LOGIC ---
           // Check for students enrolled *in the current academic year*.
           if (newStatus == ClassroomStatus.ARCHIVED) {
               AcademicYear currentYear = academicYearRepository.findByIsCurrent(true)
                       .orElse(null); // Be null-safe

               if (currentYear != null && enrollmentRepository.countByClassroomAndAcademicYear(classroom, currentYear) > 0) {
                   throw new IllegalStateException("Cannot archive a classroom with students currently enrolled. Please transfer students first.");
               }
           }
           classroom.setStatus(newStatus);
           Classroom updatedClassroom = classroomRespository.save(classroom);
           return mapToResponse(updatedClassroom);
       }
       public List<ClassroomResponseDTO> getAllClassrooms() {
         return classroomRespository.findAll()
                 .stream()
                 .map(this :: mapToResponse)
                 .toList();
       }
       public ClassroomResponseDTO getClassroomById(Long id) {
         Classroom classroom = classroomRespository.findById(id)
                 .orElseThrow(() -> new EntityNotFoundException("Classroom not founded"));

           return mapToResponse(classroom);
       }



      public List<Subject>getSubjectsByClassroom(Long classroomId) {

       Classroom classroom = classroomRespository.findById(classroomId)
               .orElseThrow(() -> new EntityNotFoundException("Classroom not found with this id: " + classroomId));
         if(teacherAssignmentRepository.findSubjectByClassroom(classroomId).isEmpty()) {
             return List.of();
         }

         return teacherAssignmentRepository.findSubjectByClassroom(classroomId);
      }


}
