package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SchoolRegistrationRequestDTO {

    @NotBlank(message = "School name is required")
    private String schoolName;

    @NotBlank(message = "schoolAddress")
    private String schoolAddress;

    @NotBlank(message = "Your full name is required")
    private String adminFullName;

    @NotBlank(message = "A valid email is required for the admin account")
    private String adminEmail;

    @Size(min = 5, max = 15, message = "Password must be between 5 and 15 characters")
    private String adminPassword;

    @Size(min = 5, max = 20, message = "Phone number must be 5+")
    private String phoneNumber;


    private MultipartFile logoFile;

}
