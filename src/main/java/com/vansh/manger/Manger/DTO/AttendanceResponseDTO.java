package com.vansh.manger.Manger.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AttendanceResponseDTO {
    private Long attendanceId;
    private Long studentSubjectId;
    private LocalDate date;
    private boolean present;
}
