package com.vansh.manger.Manger.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AcademicYearCompletionResult {
    
    private Long academicYearId;
    private String academicYearName;
    private int totalStudents;
    private LocalDate completionDate;
}
