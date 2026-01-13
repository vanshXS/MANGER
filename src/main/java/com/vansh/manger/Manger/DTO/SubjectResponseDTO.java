package com.vansh.manger.Manger.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectResponseDTO {


    private Long id;
    private String name;
    private String code;

    private boolean mandatory;

    public SubjectResponseDTO(Long id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    private long assignmentCount;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SubjectAssignmentDetailDTO> subjectAssignmentDetailDTOS;


}
