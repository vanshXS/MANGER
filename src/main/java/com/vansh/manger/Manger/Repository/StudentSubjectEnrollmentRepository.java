package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.Student;
import com.vansh.manger.Manger.Entity.StudentSubjectEnrollment;
import com.vansh.manger.Manger.Entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentSubjectEnrollmentRepository extends JpaRepository<com.vansh.manger.Manger.Entity.StudentSubjectEnrollment, Long> {

    boolean existsByStudentAndSubject(Student student, Subject subject);

    List<StudentSubjectEnrollment> findByStudentId(Long studentId);

    void deleteByStudentAndSubject(Student student, Subject subject);

    Optional<StudentSubjectEnrollment> findByStudentAndSubject(Student student, Subject subject);

    void deleteByStudentId(Long studentId);
}
