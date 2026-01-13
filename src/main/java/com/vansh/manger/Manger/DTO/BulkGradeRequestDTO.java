package com.vansh.manger.Manger.DTO;

import lombok.Data;

import java.util.List;

@Data
public class BulkGradeRequestDTO {

    private Long subjectId;
    private List<MarksRequestDTO> grades;
}
