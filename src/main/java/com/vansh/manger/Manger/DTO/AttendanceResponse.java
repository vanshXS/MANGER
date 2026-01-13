package com.vansh.manger.Manger.DTO;

import com.vansh.manger.Manger.Entity.StudentSubjectMarks;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor
public class AttendanceResponse {

    private Long attendanceId;
    private StudentSubjectMarks studentSubject;
    private LocalDate localDate;
    private boolean present;
}
