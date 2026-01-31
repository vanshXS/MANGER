package com.vansh.manger.Manger.DTO;

import com.vansh.manger.Manger.Entity.PromotionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentPromotionDTO {
    @NotNull(message = "Student ID is required")
    private Long studentId;
    /** Required when type is PROMOTE or REPEAT; optional for DROPOUT and GRADUATE. */
    private Long targetClassroomId;
    @NotNull(message = "Promotion type is required")
    private PromotionType type;
}
