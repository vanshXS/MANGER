package com.vansh.manger.Manger.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class AdminResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String role;
}
