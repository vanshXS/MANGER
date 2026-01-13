package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarksRequestDTO {

    @NotNull(message = "Student ID is required")
    private Long studentId;
    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotBlank(message = "Exam name cannot be blank")
    private String examName;

    @NotNull(message = "Marks are required")
    @DecimalMin(value = "0.0", message = "Marks cannot be less than 0")
    @DecimalMax(value = "100.0", message = "Marks cannot exceed 100")
    private Double marksObtained;

    @DecimalMin(value = "0.0", message = "Marks cannot be less than 0")
    @DecimalMax(value = "100.0", message = "Marks cannot exceed 100")
    private Double totalMarks;




}
