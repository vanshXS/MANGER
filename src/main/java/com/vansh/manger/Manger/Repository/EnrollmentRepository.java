package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.AcademicYear;
import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.Enrollment;
import com.vansh.manger.Manger.Entity.StudentStatus;
import com.vansh.manger.Manger.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> , JpaSpecificationExecutor<Enrollment> {

    /**
     * Counts how many students are enrolled in a specific class for a specific year.
     * This is used to generate the next roll number.
     */
    long countByClassroomAndAcademicYearAndSchool_Id(Classroom classroom, AcademicYear academicYear, Long schoolId);

    /**
     * Finds the specific enrollment for a student in the current year.
     */
    Optional<Enrollment> findByStudentAndSchool_IdAndAcademicYearIsCurrent(Student student, Long school_Id, boolean isCurrent);

    /**
     * Finds all enrollments for a specific classroom and academic year.
     */
    List<Enrollment> findByClassroomAndSchool_IdAndAcademicYear(Classroom classroom, Long schoolId, AcademicYear academicYear);

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


    List<Enrollment> findByClassroomAndAcademicYearAndStatus(Classroom classroom, AcademicYear year, StudentStatus status);

    boolean existsByStudentAndAcademicYear(Student student , AcademicYear academicYear);

    Optional<Enrollment> findByStudentAndAcademicYear(Student student, AcademicYear oldYear);

    List<Enrollment> findByStudentId(Long studentId);
}


