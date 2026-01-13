package com.vansh.manger.Manger.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassroomEnrollmentDTO {

    private String name;
    private int capacity;
    private int enrolled;
}
