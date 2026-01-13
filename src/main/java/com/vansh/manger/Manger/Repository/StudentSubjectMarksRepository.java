package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.Student;
import com.vansh.manger.Manger.Entity.StudentSubjectMarks;
import com.vansh.manger.Manger.Entity.Subject;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSubjectMarksRepository extends JpaRepository<StudentSubjectMarks, Long> {

    Optional<StudentSubjectMarks> findByStudentAndSubjectAndExamName(Student student, Subject subject, String examName);
    List<StudentSubjectMarks> findByStudentAndExamName(Student student, String examName);
    List<StudentSubjectMarks> findBySubjectAndExamName(Subject subject, String examName);
    @Transactional
    void deleteByStudentId(Long studentId);

    /**
     * Checks if an assignment already exists between a student and a subject.
     */
    boolean existsByStudentAndSubject(Student student, Subject subject);

    /**
     * Finds the specific assignment link between a student and a subject.
     */
    Optional<StudentSubjectMarks> findByStudentAndSubject(Student student, Subject subject);

    /**
     * Finds all elective subjects for a single student.
     * Used in the Student Profile.
     */
    List<StudentSubjectMarks> findByStudentId(Long studentId);
}
