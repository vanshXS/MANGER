package com.vansh.manger.Manger.DTO;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SchoolProfileDTO {

    private Long id; // Read-only

    @Size(min = 3, max = 100, message = "School name must be between 3 and 100 characters")
    private String name;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

    private String logoUrl;


}