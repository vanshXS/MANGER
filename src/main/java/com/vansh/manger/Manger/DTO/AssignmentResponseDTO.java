package com.vansh.manger.Manger.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AssignmentResponseDTO {

  private  Long assignmentId;
  private  Long teacherId;
   private String teacherName;
   private Long subjectId;
   private String subjectName;
   private Long classroomId;
   private String classroomName;

   private boolean mandatory;


}
