package com.vansh.manger.Manger.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardKpiDTO {
    private long totalStudents;
    private long activeTeachers;
    private int classroomUtilization;
    private long unassignedTeachers;
    private String message;
}
