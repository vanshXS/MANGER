package com.vansh.manger.Manger.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TeacherAssignmentDTO {

    private Long assignmentId;

    private Long classroomId;
    private String className;

    private Long teacherId;
    private String teacherName;

    private Long subjectId;
    private String subjectName;

    private boolean mandatory;
}
