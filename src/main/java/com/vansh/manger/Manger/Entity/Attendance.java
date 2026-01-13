package com.vansh.manger.Manger.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "attendances" ,
uniqueConstraints = @UniqueConstraint(columnNames = {"student_subject_id, date"}))
@Data
@AllArgsConstructor @NoArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_subject_id", nullable = false)
    private StudentSubjectMarks studentSubject;

    @Column(name = "date", nullable = false)
    private LocalDate localDate;
    @Column(nullable = false)
    private boolean present;
}
