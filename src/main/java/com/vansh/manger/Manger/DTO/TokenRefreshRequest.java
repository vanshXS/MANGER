package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token cannot be null")
    private String refreshToken;
}
