package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeacherRespository extends JpaRepository<Teacher, Long>, JpaSpecificationExecutor<Teacher> {

    boolean existsByEmail(String email);
    Optional<Teacher>findByEmail(String email);

    @Query("SELECT t FROM Teacher t WHERE t.id NOT IN (SELECT ta.teacher.id FROM TeacherAssignment ta WHERE ta.teacher IS NOT NULL)")
    List<Teacher> findUnassignedTeachers();

}
