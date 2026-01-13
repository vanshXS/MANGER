package com.vansh.manger.Manger.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;

@Data
public class ResetPasswordRequest {

    @Column(nullable = false, unique = true)
    @Email(message = "Email must be in email formatted")
    private String email;

    @NotBlank(message = "Old password required")
    private String oldPassword;
    @NotBlank(message = "Password cannot be null")
    @Size(min = 2 , max = 5, message = "Password must be between length 2 and 5")
    private String newPassword;
}
