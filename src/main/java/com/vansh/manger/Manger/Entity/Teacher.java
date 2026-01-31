package com.vansh.manger.Manger.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "teachers" ,
  indexes = {
        @Index(name = "idx_teacher_active" , columnList = "active"),
          @Index(name = "idx_teacher_email", columnList = "email"),
          @Index(name = "idx_teacher_active_id", columnList = "active, id")
  }


)
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;


    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    private boolean active = true;

    @NotBlank(message = "Password is required")

    private String password;

    private String profilePictureUrl;

    private LocalDate joiningDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Enumerated(EnumType.STRING)
    private Roles role;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @PrePersist
    public void onCreate() {
        this.joiningDate = LocalDate.now();
    }
}
