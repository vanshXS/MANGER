package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentRequestDTO {

    @NotNull(message = "Classroom ID is required")
    private Long classroomId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private boolean mandatory;
}
