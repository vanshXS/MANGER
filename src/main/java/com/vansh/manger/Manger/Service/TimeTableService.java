package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.TeacherResponseDTO;
import com.vansh.manger.Manger.DTO.TimeTableRequestDTO;
import com.vansh.manger.Manger.DTO.TimeTableResponseDTO;
import com.vansh.manger.Manger.Entity.*;
import com.vansh.manger.Manger.Repository.*;
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
   private final SubjectRepository subjectRepository;

    public TimeTableResponseDTO mapToResponse(TimeTable timeTable) {

        TimeTableResponseDTO dto = TimeTableResponseDTO.builder()
                .id(timeTable.getId())
                .teacherName(timeTable.getTeacherAssignment().getTeacher().getFirstName() + " " + timeTable.getTeacherAssignment().getTeacher().getLastName())
                .subjectName(timeTable.getTeacherAssignment().getSubject().getName())
                .classroom(timeTable.getTeacherAssignment().getClassroom().getName())
                .day(String.valueOf(timeTable.getDay()))
                .startTime(timeTable.getStartTime())
                .endTime(timeTable.getEndTime())
                .build();

        return dto;
    }

    public TimeTableResponseDTO createTimeTable(TimeTableRequestDTO requestDTO) {

        TeacherAssignment teacherAssignment = teacherAssignmentRepository.findById(requestDTO.getTeacherAssignmentId())
                .orElseThrow(() -> new RuntimeException("No valid assignment found for this teacher, subject, and classroom combination. Please assign first!"));

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
                .build();

        TimeTable saved = timeTableRepository.save(timeTable);

        return mapToResponse(saved);
    }



    //update timetable
    public TimeTableResponseDTO updateTimeTable(Long timetableId, TimeTableRequestDTO update) {

        TimeTable existedTimeTable = timeTableRepository.findById(timetableId)
                .orElseThrow(() -> new RuntimeException("Timetable not existed"));

       TeacherAssignment teacherAssignment = teacherAssignmentRepository.findById(update.getTeacherAssignmentId())
               .orElseThrow(() -> new RuntimeException("No valid assignment found for this teacher, subject, and classroom combination. Please assign first!"));

        DayOfWeek day = DayOfWeek.valueOf(update.getDay().toUpperCase());

        //validation : teacher busy?
        boolean teacherBusy = timeTableRepository.existsByTeacherAssignment_Teacher_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(teacherAssignment.getTeacher().getId(), day, update.getEndTime(), update.getStartTime());

        if(teacherBusy) {
            throw new RuntimeException("Teacher is already scheduled at this time. ");
        }

        boolean classBusy = timeTableRepository.existsByTeacherAssignment_Classroom_IdAndDayAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(teacherAssignment.getClassroom().getId(),day, update.getEndTime(), update.getStartTime());

        if(classBusy) {
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
        TimeTable timeTable = timeTableRepository.findById(timeTableId)
                .orElseThrow(() -> new RuntimeException("Timetable not found"));

        timeTableRepository.delete(timeTable);

    }

    //get all timetable
    public List<TimeTableResponseDTO> getAll() {
        return timeTableRepository.findAll()
                .stream()
                .map(this :: mapToResponse)
                .toList();
    }

    public List<TimeTableResponseDTO> getByTeacherId(Long teacherId) {
        Teacher teacher = teacherRespository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return timeTableRepository.findByTeacherAssignment_Teacher_Id(teacher.getId())
                .stream()
                .map(this :: mapToResponse)
                .toList();
    }


    // FROM TEACHER PERSPECTIVE : FUNCTIONALITIES

    //get timetable
   public List<TimeTableResponseDTO> getMyTimeTable(String email) {
        Teacher teacher = teacherRespository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return timeTableRepository.findByTeacherAssignment_Teacher_Id(teacher.getId())
                .stream()
                .map(this :: mapToResponse)
                .toList();
   }

    public List<TimeTableResponseDTO> getByClassroomId(Long classroomId) {
        return timeTableRepository.findByTeacherAssignment_Classroom_Id(classroomId)
                .stream()
                .map(this :: mapToResponse)
                .toList();
    }

}
