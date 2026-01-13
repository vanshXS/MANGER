package com.vansh.manger.Manger.Specification;

import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.Student;
import com.vansh.manger.Manger.Entity.Teacher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * A single file to hold all JPA Specifications for the global search feature.
 * This keeps related search logic consolidated and easy to manage.
 */

public class SearchSpecification {

    /**
     * Creates a Specification to search for Students by their full name.
     * @param query The search term.
     * @return A Specification for the Student entity.
     */

    public static Specification<Student> studentNameLike(String query) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                // Concatenates firstName and lastName with a space for searching.
                                criteriaBuilder.concat(root.get("firstName"), " ")
                        ),
                        "%" + query.toLowerCase() + "%"
                );
    }

    /**
     * Creates a Specification to search for Teachers by their full name.
     * @param query The search term.
     * @return A Specification for the Teacher entity.
     */

    public static Specification<Teacher> teacherNameLike(String query) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                criteriaBuilder.concat(root.get("firstName"), " ")
                        ),
                        "%" + query.toLowerCase() + "%"
                );
    }

    /**
     * Creates a Specification to search for Classrooms by their name.
     * @param query The search term.
     * @return A Specification for the Classroom entity.
     */

    public static Specification<Classroom> classroomNameLike(String query) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + query.toLowerCase() + "%"
                );
    }
}