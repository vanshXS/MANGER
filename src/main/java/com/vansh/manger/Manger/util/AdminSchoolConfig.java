package com.vansh.manger.Manger.util;

import com.vansh.manger.Manger.Entity.School;
import com.vansh.manger.Manger.Entity.User;
import com.vansh.manger.Manger.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdminSchoolConfig {

    private final UserRepo userRepo;

    /**
     * ❗ Use ONLY when school is mandatory (admin/teacher/student APIs)
     */
    public School requireCurrentSchool() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "No authenticated user found. School context is required."
            );
        }

        String email = authentication.getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getSchool() == null) {
            throw new IllegalStateException("User is not associated with any school");
        }

        return user.getSchool();
    }

    /**
     * ✅ Safe method — returns null instead of crashing
     */
    public School getOptionalCurrentSchool() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return userRepo.findByEmail(authentication.getName())
                .map(User::getSchool)
                .orElse(null);
    }

    /**
     * Gets the current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        String email = authentication.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
