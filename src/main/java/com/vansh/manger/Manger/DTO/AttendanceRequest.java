package com.vansh.manger.Manger.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceRequest {

    private Long studentSubjectId;
    private LocalDate localDate;
    private boolean present;
}
