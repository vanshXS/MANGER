package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.Attendance;
import com.vansh.manger.Manger.Entity.StudentSubjectMarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {


    boolean existsByStudentSubjectAndLocalDate(StudentSubjectMarks ss, LocalDate date);

    List<Attendance> findByStudentSubjectId(Long studentSubjectId);
}
