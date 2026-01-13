package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.DTO.AssignmentResponseDTO;
import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.Subject;
import com.vansh.manger.Manger.Entity.Teacher;
import com.vansh.manger.Manger.Entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {

    Optional<TeacherAssignment>findByTeacherAndClassroom(Teacher teacher, Classroom classroom);
    boolean existsByTeacherAndClassroom(Teacher teacher, Classroom classroom);

    boolean existsByClassroomAndSubject(Classroom classroom, Subject subject);

    void deleteByTeacher(Teacher teacher);

   Optional<TeacherAssignment>findByClassroomAndSubject(Classroom classroom, Subject subject);

   @Query(
           value = "SELECT s.* FROM subjects s " +
                   "JOIN teacher_assignment ta ON s.id = ta.subject_id " +
                    "WHERE ta.classroom_id = :classroom_id",
           nativeQuery = true
   )
   List<Subject> findSubjectByClassroom(@Param("classroom_id") Long classroom_id);

    List<TeacherAssignment> findByTeacher(Teacher teacher);

    Optional<TeacherAssignment> findByTeacher_IdAndSubject_IdAndClassroom_Id(Long teacherId, Long subjectId, Long classroomId);

    long countByTeacher(Teacher teacher);

    boolean existsBySubject(Subject subject);

    long countBySubject(Subject subject);

    boolean existsByClassroomAndSubjectAndTeacherIsNull(Classroom classroom, Subject subject);

    List<TeacherAssignment> findByClassroomId(Long classroomId);
    List<TeacherAssignment> findBySubject(Subject subject);

    boolean existsByClassroom(Classroom classroom);

    List<TeacherAssignment> findByClassroomAndMandatoryTrue(Classroom classroom);

    boolean existsByClassroomAndSubjectAndMandatoryTrue(
            Classroom classroom,
            Subject subject
    );

}
