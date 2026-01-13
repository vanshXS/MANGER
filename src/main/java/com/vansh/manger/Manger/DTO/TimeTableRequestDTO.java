package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class TimeTableRequestDTO {

    @NotNull(message = "Assignment ID is required")
   private Long teacherAssignmentId;
    @NotBlank(message = "Day is required")
    private String day;
    @NotNull(message = "start time is required")
    private LocalTime startTime;

    @NotNull(message = "end time is required")
    private LocalTime endTime;
}
