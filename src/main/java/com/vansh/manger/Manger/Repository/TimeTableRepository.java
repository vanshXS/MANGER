package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.*;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {

 List<TimeTable> findByTeacherAssignment_Teacher_Id(Long teacherId);
 List<TimeTable> findByTeacherAssignment_Classroom_Id(Long classroomId);

    boolean existsByTeacherAssignment_Teacher_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(Long teacherId, DayOfWeek day, LocalTime endTime, LocalTime startTime);

    boolean existsByTeacherAssignment_Classroom_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(Long classroomId, DayOfWeek day, LocalTime endTime, LocalTime startTime);
}
