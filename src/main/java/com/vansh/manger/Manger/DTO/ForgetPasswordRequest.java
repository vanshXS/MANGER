package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForgetPasswordRequest {

    @Email(message = "Email must be in valid format")
    @NotNull(message = "Email must be filled")
    private String email;
}
