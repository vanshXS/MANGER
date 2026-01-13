package com.vansh.manger.Manger.Repository;

import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.ClassroomSubject;
import com.vansh.manger.Manger.Entity.Subject;
import com.vansh.manger.Manger.Entity.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomSubjectRepository
        extends JpaRepository<ClassroomSubject, Long> {

    List<ClassroomSubject> findByClassroomAndSubjectType(
            Classroom classroom,
            SubjectType subjectType
    );

    Optional<ClassroomSubject> findByClassroomAndSubject(
            Classroom classroom,
            Subject subject
    );
}
