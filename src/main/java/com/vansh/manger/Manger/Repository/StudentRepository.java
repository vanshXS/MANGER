package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.School;
import com.vansh.manger.Manger.Entity.Student;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    /**
     * Finds all students in a school
     */
    List<Student> findBySchool_Id(Long schoolId);

    Optional<Student> findByEmailAndSchool_Id(String email, Long schoolId);

    Page<Student> findBySchool_Id(Long schoolId, Pageable pageable, Specification<?> specs);

    Optional<Student> findByIdAndSchool_Id(Long studentId, Long schoolId);

    boolean existsByEmailAndSchool_Id(String email, Long school_id);

}