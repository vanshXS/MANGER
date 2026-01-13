package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminLoginDTO {

    @Email(message = "Email should be valid email")
    @NotBlank(message = "Email is required")

    private String email;

    @Size(min = 5, message = "Password must be at least 5 characters")
    private String password;
}
