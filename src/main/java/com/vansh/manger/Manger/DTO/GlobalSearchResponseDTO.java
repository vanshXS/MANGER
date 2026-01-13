package com.vansh.manger.Manger.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor @NoArgsConstructor
public class GlobalSearchResponseDTO {

    private List<SearchResultDTO> students;
    private List<SearchResultDTO> teachers;
    private List<SearchResultDTO> classrooms;

}
