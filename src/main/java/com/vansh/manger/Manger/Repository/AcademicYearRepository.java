package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    Optional<AcademicYear> findByIsCurrentAndSchool_Id(boolean isCurrent, Long schoolId);

    List<AcademicYear> findBySchool_IdOrderByStartDateDesc(Long schoolId);

    boolean existsBySchool_IdAndName(Long schoolId, String name);

    @Modifying
    @Query("""
    UPDATE AcademicYear a 
    SET a.isCurrent = false 
    WHERE a.school.id = :schoolId AND a.isCurrent = true
             """)
    int unsetAllCurrentYearsBySchool(Long schoolId);


    Optional<AcademicYear> findByIdAndSchool_Id(Long yearId, Long schoolId);

    Optional<AcademicYear> findByClosedAndSchool_Id(boolean isClosed, Long schoolId);

    /** Returns the most recently closed academic year (by end date). Used for promotion from "previous" year. */
    Optional<AcademicYear> findTopByClosedTrueAndSchool_IdOrderByEndDateDesc(Long schoolId);

}
