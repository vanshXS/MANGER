package com.vansh.manger.Manger.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherWorkloadDTO {

    private String name;
    private long assignedClassesCount;
}
