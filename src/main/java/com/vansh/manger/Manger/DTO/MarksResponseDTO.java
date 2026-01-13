package com.vansh.manger.Manger.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
@Builder
public class MarksResponseDTO {
    private Long id;
    private String studentName;
    private String subjectName;
    private String examName;
    private Double marksObtained;
    private Double totalMarks;
    private Double percentage;
    private String grade;

}
