package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    Optional<School>findByName(String name);
    boolean existsByNameAndAddress(String schoolName, String address);
}
