package com.vansh.manger.Manger.DTO;

import com.vansh.manger.Manger.Entity.ClassroomStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassroomRequestDTO {
    @NotBlank(message = "Classroom name is required")
    @Size(min = 2, max = 50, message = "Class name must be between 2 and 20 characters")
    private String name;

    @Min(value = 1, message = "Capacity name is required")
    private Integer capacity;


}
