package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {

    /** All timetables for a school (use for admin getAll). */
    List<TimeTable> findBySchool_Id(Long schoolId);

    /** Get one timetable by id only if it belongs to the school (secure update/delete). */
    Optional<TimeTable> findByIdAndSchool_Id(Long id, Long schoolId);

    List<TimeTable> findByTeacherAssignment_Teacher_Id(Long teacherId);
    List<TimeTable> findByTeacherAssignment_Classroom_Id(Long classroomId);

    boolean existsByTeacherAssignment_Teacher_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(Long teacherId, DayOfWeek day, LocalTime endTime, LocalTime startTime);
    boolean existsByTeacherAssignment_Classroom_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(Long classroomId, DayOfWeek day, LocalTime endTime, LocalTime startTime);
}
