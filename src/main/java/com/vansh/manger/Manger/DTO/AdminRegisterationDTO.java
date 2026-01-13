package com.vansh.manger.Manger.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminRegisterationDTO {

    @NotBlank(message = "fullName is required")
    @Size(min = 3, max = 20)
    private String fullName;
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

   @Size(min = 5, max = 15, message = "Password must be between 5 and 15 characters")
    private String password;
}
