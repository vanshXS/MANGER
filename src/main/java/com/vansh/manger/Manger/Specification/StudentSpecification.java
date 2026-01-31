package com.vansh.manger.Manger.Specification;

import com.vansh.manger.Manger.Entity.Classroom;
import com.vansh.manger.Manger.Entity.Enrollment;
import com.vansh.manger.Manger.Entity.Student;
import com.vansh.manger.Manger.Entity.StudentStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class StudentSpecification {

// Filter by Enrollment Status
    public static Specification<Student> hasStatus(StudentStatus status) {

        return (root, query, cb) -> {
            if(status == null) return null;

            Join<Student, Enrollment> enrollmentJoin = root.join("enrollments", JoinType.INNER);

            return cb.and(
                    cb.equal(enrollmentJoin.get("status"), status),
                    cb.isTrue(enrollmentJoin.get("academicYear").get("isCurrent"))
            );
        };
    }

    // Search By Student + Enrollment + Classroom

    public static Specification<Student> search(String keyword) {

        return (root, query, cb) -> {

            if(keyword == null || keyword.isBlank()) return null;

            String pattern = "%" + keyword + "%";

            //joins

            Join<Student, Enrollment> enrollmentJoin = root.join("enrollments", JoinType.INNER);
            Join<Enrollment, Classroom> classroomJoin = enrollmentJoin.join("classroom", JoinType.LEFT);

            return cb.and(
                    cb.isTrue(enrollmentJoin.get("academicYear").get("isCurrent")),
                    cb.or(
                            cb.like(cb.lower(root.get("firstName")), pattern),
                            cb.like(cb.lower(root.get("lastName")), pattern),
                            cb.like(cb.lower(root.get("email")), pattern),
                            cb.like(cb.lower(enrollmentJoin.get("rollNo")), pattern),
                            cb.like(cb.lower(classroomJoin.get("name")), pattern)
                    )
            );

        };
    }

    public static Specification<Student> hasSchoolId(Long schoolId) {
        return (root, query, cb) -> {
            if(schoolId == null) return null;
            return cb.equal(root.get("school").get("id"), schoolId);
        };
    }

    //Combine filters

    public static Specification<Student> build(StudentStatus status, String search, Long schoolId) {
        return Specification
                .where(hasStatus(status))
                .and(hasSchoolId(schoolId))
                .and(search(search));
    }
}
