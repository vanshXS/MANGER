package com.vansh.manger.Manger.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
    name = "academic_year",
    uniqueConstraints = @UniqueConstraint(columnNames = { "school_id", "name" }),
    indexes = {
        @Index(columnList = "school_id"),
        @Index(columnList = "school_id, is_current"),
        @Index(columnList = "school_id, closed")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    /** Only one academic year per school should be current at a time. */
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    /** When true, this year is closed and students can be promoted from it to the next. */
    @Column(nullable = false)
    private Boolean closed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
}