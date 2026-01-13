package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.DTO.ClassroomResponseDTO;
import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.ClassroomStatus;
import com.vansh.manger.Manger.Entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRespository extends JpaRepository<Classroom, Long>, JpaSpecificationExecutor<Classroom> {

    // --- NEW SCHOOL-SCOPED METHODS ---

    /** Finds all classrooms for a specific school with a specific status. */
    List<Classroom> findBySchoolAndStatus(School school, ClassroomStatus status);

    /** Checks if a classroom name exists WITHIN a specific school. */
    boolean existsByNameAndSchool(String name, School school);

    /** Finds a classroom by name WITHIN a specific school. */
    Optional<Classroom> findByNameAndSchool(String name, School school);

    /** Finds a classroom by ID and ensures it belongs to the given school. */
    Optional<Classroom> findByIdAndSchool(Long id, School school);
}
