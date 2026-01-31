package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.Subject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByNameIgnoreCaseAndSchool_Id(String name, Long schoolId);
    boolean existsByCodeIgnoreCaseAndSchool_Id(String code, Long schoolId);

    List<Subject> findBySchool_Id(Long schoolId);
    Optional<Subject> findByIdAndSchool_Id(Long subjectId, Long schoolId);

}
