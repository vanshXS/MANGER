package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PromotionRequestDTO {
    @NotNull(message = "Source classroom ID is required")
    private Long fromClassroomId;
    @NotNull(message = "Target classroom ID is required")
    private Long toClassroomId;
}
