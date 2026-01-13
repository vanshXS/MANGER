package com.vansh.manger.Manger.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data

@NoArgsConstructor @AllArgsConstructor
@Builder
public class TeacherResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;
    private String phoneNumber;

    private String joinDate;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TeacherAssignmentDTO> assignedClassrooms;

    private boolean active;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profilePictureUrl;

}
