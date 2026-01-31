package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherRespository extends JpaRepository<Teacher, Long>, JpaSpecificationExecutor<Teacher> {

    boolean existsByEmailAndSchool_Id(String email, Long schoolId);

    Optional<Teacher>findByEmailAndSchool_Id(String email, Long schoolId);


    @Query("""
SELECT t FROM Teacher t
WHERE t.school.id = :schoolId
AND t.id NOT IN (
    SELECT ta.teacher.id
    FROM TeacherAssignment ta
    WHERE ta.teacher IS NOT NULL
    AND ta.teacher.school.id = :schoolId
)
""")
    List<Teacher> findUnassignedTeachersBySchool_Id(@Param("schoolId") Long schoolId);


    Page<Teacher> findBySchool_Id(Pageable pageable, Long schoolId);

    Optional<Teacher> findByIdAndSchool_Id(Long teacherId, Long schoolId);

    Page<Teacher> findBySchool_Id(Long schoolId, Specification<?> specs, Pageable pageable);

    List<Teacher> findBySchool_Id(Long schoolId);



}
