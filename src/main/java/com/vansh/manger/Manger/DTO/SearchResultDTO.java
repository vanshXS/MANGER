package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class SearchResultDTO {
    private Long id;
    private String name;
    private String subtitle;
    private String type;

    public SearchResultDTO(Long id, @NotBlank(message = "Classroom name is required") @Size(min = 2, max = 50, message = "Class name must be between 2 and 50 characters") String name, String s, String classroom, String path) {
    }
}
