package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.TeacherResponseDTO;
import com.vansh.manger.Manger.DTO.TimeTableRequestDTO;
import com.vansh.manger.Manger.DTO.TimeTableResponseDTO;
import com.vansh.manger.Manger.Entity.*;
import com.vansh.manger.Manger.Repository.*;
import com.vansh.manger.Manger.util.AdminSchoolConfig;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeTableService {

    private final TimeTableRepository timeTableRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherRespository teacherRespository;
    private final ClassroomRespository classroomRespository;
    private final AdminSchoolConfig adminSchoolConfig;

    public TimeTableResponseDTO mapToResponse(TimeTable timeTable) {

       return TimeTableResponseDTO.builder()
                .id(timeTable.getId())
                .teacherName(timeTable.getTeacherAssignment().getTeacher().getFirstName() + " " + timeTable.getTeacherAssignment().getTeacher().getLastName())
                .subjectName(timeTable.getTeacherAssignment().getSubject().getName())
                .classroom(timeTable.getTeacherAssignment().getClassroom().getName())
                .day(String.valueOf(timeTable.getDay()))
                .startTime(timeTable.getStartTime())
                .endTime(timeTable.getEndTime())
                .build();


    }

    @Transactional
    public TimeTableResponseDTO createTimeTable(TimeTableRequestDTO requestDTO) {
        School school = adminSchoolConfig.requireCurrentSchool();

        TeacherAssignment teacherAssignment = teacherAssignmentRepository.findById(requestDTO.getTeacherAssignmentId())
                .orElseThrow(() -> new RuntimeException("No valid assignment found for this teacher, subject, and classroom combination. Please assign first!"));

        if (!teacherAssignment.getClassroom().getSchool().getId().equals(school.getId())) {
            throw new IllegalArgumentException("Assignment does not belong to your school.");
        }

        DayOfWeek day = DayOfWeek.valueOf(requestDTO.getDay().toUpperCase());

        //validation : teacher busy?
        boolean teacherBusy = timeTableRepository.existsByTeacherAssignment_Teacher_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(teacherAssignment.getTeacher().getId(), day, requestDTO.getEndTime(), requestDTO.getStartTime());

        if(teacherBusy) {
            throw new RuntimeException("Teacher is already scheduled at this time. ");
        }

        boolean classBusy = timeTableRepository.existsByTeacherAssignment_Classroom_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(teacherAssignment.getClassroom().getId(),day, requestDTO.getEndTime(), requestDTO.getStartTime());

        if(classBusy) {
            throw new RuntimeException("Classroom is already booked at this time. ");

        }

        TimeTable timeTable = TimeTable.builder()
                .teacherAssignment(teacherAssignment)
                .day(day)
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .school(school)
                .build();

        TimeTable saved = timeTableRepository.save(timeTable);

        return mapToResponse(saved);
    }



    //update timetable
    @Transactional
    public TimeTableResponseDTO updateTimeTable(Long timetableId, TimeTableRequestDTO update) {
        School school = adminSchoolConfig.requireCurrentSchool();

        TimeTable existedTimeTable = timeTableRepository.findByIdAndSchool_Id(timetableId, school.getId())
                .orElseThrow(() -> new RuntimeException("Timetable not found or does not belong to your school."));

        TeacherAssignment teacherAssignment = teacherAssignmentRepository.findById(update.getTeacherAssignmentId())
                .orElseThrow(() -> new RuntimeException("No valid assignment found for this teacher, subject, and classroom combination. Please assign first!"));

        if (!teacherAssignment.getClassroom().getSchool().getId().equals(school.getId())) {
            throw new IllegalArgumentException("Assignment does not belong to your school.");
        }

        DayOfWeek day = DayOfWeek.valueOf(update.getDay().toUpperCase());

        boolean teacherBusy = timeTableRepository.existsByTeacherAssignment_Teacher_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(teacherAssignment.getTeacher().getId(), day, update.getEndTime(), update.getStartTime());
        if (teacherBusy) {
            throw new RuntimeException("Teacher is already scheduled at this time. ");
        }

        boolean classBusy = timeTableRepository.existsByTeacherAssignment_Classroom_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(teacherAssignment.getClassroom().getId(), day, update.getEndTime(), update.getStartTime());
        if (classBusy) {
            throw new RuntimeException("Classroom is already booked at this time. ");
        }

        existedTimeTable.setTeacherAssignment(teacherAssignment);
        existedTimeTable.setStartTime(update.getStartTime());
        existedTimeTable.setEndTime(update.getEndTime());
        existedTimeTable.setDay(day);

        TimeTable updated = timeTableRepository.save(existedTimeTable);
        return mapToResponse(updated);
    }
          //delete timetable
    public void deleteTimeTable(Long timeTableId) {
        School school = adminSchoolConfig.requireCurrentSchool();
        TimeTable timeTable = timeTableRepository.findByIdAndSchool_Id(timeTableId, school.getId())
                .orElseThrow(() -> new RuntimeException("Timetable not found or does not belong to your school."));
        timeTableRepository.delete(timeTable);
    }

    //get all timetable (school-scoped)
    @Transactional
    public List<TimeTableResponseDTO> getAll() {
        School school = adminSchoolConfig.requireCurrentSchool();
        return timeTableRepository.findBySchool_Id(school.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TimeTableResponseDTO> getByTeacherId(Long teacherId) {
        School school = adminSchoolConfig.requireCurrentSchool();
        Teacher teacher = teacherRespository.findByIdAndSchool_Id(teacherId, school.getId())
                .orElseThrow(() -> new RuntimeException("Teacher not found or does not belong to your school."));
        return timeTableRepository.findByTeacherAssignment_Teacher_Id(teacher.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // FROM TEACHER PERSPECTIVE : FUNCTIONALITIES

    //get timetable
   public List<TimeTableResponseDTO> getMyTimeTable(String email) {
        Teacher teacher = teacherRespository.findByEmailAndSchool_Id(email, adminSchoolConfig.requireCurrentSchool().getId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return timeTableRepository.findByTeacherAssignment_Teacher_Id(teacher.getId())
                .stream()
                .map(this :: mapToResponse)
                .toList();
   }

    public List<TimeTableResponseDTO> getByClassroomId(Long classroomId) {
        School school = adminSchoolConfig.requireCurrentSchool();
        classroomRespository.findByIdAndSchool(classroomId, school)
                .orElseThrow(() -> new RuntimeException("Classroom not found or does not belong to your school."));
        return timeTableRepository.findByTeacherAssignment_Classroom_Id(classroomId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

}
