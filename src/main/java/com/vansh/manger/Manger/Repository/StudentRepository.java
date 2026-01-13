package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.Student;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {


    @Query("SELECT s FROM Student s WHERE LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Student> searchByName(@Param("query") String query);



    Optional<Student>findByEmail(String email);





    Page<Student> findAll(Pageable pageable);


    boolean existsByEmail(String email);






}