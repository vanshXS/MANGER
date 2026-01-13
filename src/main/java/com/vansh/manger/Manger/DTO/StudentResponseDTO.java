package com.vansh.manger.Manger.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Represents a Student's profile AND their *current* enrollment status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponseDTO {

    // --- Student Details ---
    private Long id; // This is the Student's permanent ID
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profilePictureUrl;

    // --- Current Enrollment Details ---
    // These fields come from the *current* Enrollment record
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long currentEnrollmentId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String rollNo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ClassroomResponseDTO classroomResponseDTO; // Their current classroom
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String academicYearName; // e.g., "2024-2025"

    // --- Academic Details ---
    private List<SubjectResponseDTO> subjectResponseDTOS; // Their individually assigned subjects

    // --- For Create ONLY ---
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password; // Only sent once upon creation
}