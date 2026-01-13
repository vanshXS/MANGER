package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class AcademicYearDTO {

    private Long id; // Used for responses

    @NotBlank(message = "Year name is required (e.g., 2024-2025)")
    private String name;

    @NotNull(message = "Start date is required")
    private LocalDate startDate; // Will accept "YYYY-MM-DD"

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private boolean isCurrent; // Used for responses
}