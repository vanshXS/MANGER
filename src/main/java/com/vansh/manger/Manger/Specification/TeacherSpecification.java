package com.vansh.manger.Manger.Specification;

import com.vansh.manger.Manger.Entity.Teacher;
import org.springframework.data.jpa.domain.Specification;

public class TeacherSpecification {

    public static Specification<Teacher> hasStatus(Boolean active) {

        return (root, query, cb) -> {
            if(active == null) return null;

            return cb.equal(root.get("active"), active);
        };
    }

    public static Specification<Teacher> search(String keyword) {

        return (root,query, cb) -> {

            if(keyword == null || keyword.trim().isEmpty()) {
                return null; //ignored
            }

            String pattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(

                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }

    public static Specification<Teacher> hasSchool(Long schoolId) {
        return (root, query, cb) -> {
            if(schoolId == null) return null;

           return cb.equal(root.get("school").get("id"), schoolId);
        };
    }

    public static Specification<Teacher> build(Boolean active, String search, Long schoolId) {
        return Specification
                .where(hasStatus(active))
                .and(
                        hasSchool(schoolId)
                )
                .and(search(search));

    }
}
