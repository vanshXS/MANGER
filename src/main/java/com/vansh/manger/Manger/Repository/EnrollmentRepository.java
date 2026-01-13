package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.AcademicYear;
import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.Enrollment;
import com.vansh.manger.Manger.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Counts how many students are enrolled in a specific class for a specific year.
     * This is used to generate the next roll number.
     */
    long countByClassroomAndAcademicYear(Classroom classroom, AcademicYear academicYear);

    /**
     * Finds the specific enrollment for a student in the current year.
     */
    Optional<Enrollment> findByStudentAndAcademicYearIsCurrent(Student student, boolean isCurrent);

    /**
     * Finds all enrollments for a specific student (their history).
     */
    List<Enrollment> findByStudent(Student student);

    /**
     * Finds all enrollments for a specific classroom and academic year.
     */
    List<Enrollment> findByClassroomAndAcademicYear(Classroom classroom, AcademicYear academicYear);

    /**
     * Deletes all enrollments for a given student ID.
     * Part of the delete cascade.
     */
    @Transactional
    void deleteByStudentId(Long studentId);


    long countByAcademicYear(AcademicYear currentYear);

    boolean existsByClassroom(Classroom classroom);

    List<Enrollment> findByClassroomId(Long classroomId);

    Optional<Enrollment> findActiveByStudent(Student student);
}