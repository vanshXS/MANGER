package com.vansh.manger.Manger.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityLogDTO {

    private String description;
    private String category;
    private LocalDateTime date;
}
