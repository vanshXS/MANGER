package com.vansh.manger.Manger.DTO;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAssignmentTeacherDTO {

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
}
