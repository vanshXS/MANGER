package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgetResetPassword {

    @Email(message = "Email must be in correct format")
    private String email;
    @NotBlank
    private String otp;

    @Size(min = 2, max = 15)
    private String newPassword;
}
