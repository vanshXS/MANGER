package com.vansh.manger.Manger.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BulkAttendanceRequest {

    private LocalDate date;
    private Long classroomId;

    private List<StudentAttendanceDTO> records;
}
