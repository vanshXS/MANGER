package com.vansh.manger.Manger.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor
@Table(name = "student_subject_marks" ,
 uniqueConstraints = @UniqueConstraint(columnNames =  {"student_id", "subject_id", "exam_name"}))
@Builder
public class StudentSubjectMarks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
   private Double marksObtained;

    @Column(name = "exam_name", nullable = false)
   private String examName;

   private String grade;

   private Double totalMarks = 100.0;

   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;

   @PrePersist
    protected void onCreate() {
      this.createdAt = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   @PreUpdate
    protected void onUpdate() {
       this.updatedAt = LocalDateTime.now();
   }



}
