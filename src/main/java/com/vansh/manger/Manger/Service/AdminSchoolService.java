package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.ChangePasswordRequestDTO;
import com.vansh.manger.Manger.DTO.SchoolProfileDTO;
import com.vansh.manger.Manger.Entity.School;
import com.vansh.manger.Manger.Entity.User;
import com.vansh.manger.Manger.Repository.SchoolRepository;
import com.vansh.manger.Manger.Repository.UserRepo;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AdminSchoolService {

    private final SchoolRepository schoolRepository;
    private final ActivityLogService activityLogService;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final UserRepo userRepo;


    /* -------------------------------------------------------
     * 🔐 AUTH HELPERS
     * ------------------------------------------------------- */

    private Long getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getId();
        }

        if (principal instanceof String) {  
            String email = (String) principal;
            return userRepo.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("User not found via email."))
                    .getId();
        }

        throw new IllegalStateException("Invalid authentication principal.");
    }

    private User getAuthenticatedUserEntity() {
        Long userId = getAuthenticatedUserId();
        return userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found."));
    }


    /* -------------------------------------------------------
     * 📌 DTO MAPPER
     * ------------------------------------------------------- */
    private SchoolProfileDTO mapToDTO(School school) {
        SchoolProfileDTO dto = new SchoolProfileDTO();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setAddress(school.getAddress());
        dto.setPhoneNumber(school.getPhoneNumber());
        dto.setLogoUrl(school.getLogoUrl());
        return dto;
    }


    /* -------------------------------------------------------
     * 📌 FETCH SCHOOL PROFILE
     * ------------------------------------------------------- */
    @Transactional(readOnly = true)
    public SchoolProfileDTO getSchoolProfile() {

        User admin = getAuthenticatedUserEntity();
        School school = admin.getSchool();

        if (school == null) {
            throw new EntityNotFoundException("No school is associated with this admin.");
        }

        return mapToDTO(school);
    }


    /* -------------------------------------------------------
     * 📌 UPDATE SCHOOL INFO
     * ------------------------------------------------------- */
    @Transactional
    public SchoolProfileDTO updateSchoolProfile(SchoolProfileDTO dto) {

        User admin = getAuthenticatedUserEntity();
        School school = admin.getSchool();

        if (school == null) {
            throw new EntityNotFoundException("No school is associated with this admin.");
        }

        school.setName(dto.getName());
        school.setAddress(dto.getAddress());
        school.setPhoneNumber(dto.getPhoneNumber());

        School updated = schoolRepository.save(school);

        activityLogService.logActivity(
                admin.getFullName() + " updated school profile.",
                "Settings"
        );

        return mapToDTO(updated);
    }


    /* -------------------------------------------------------
     * 🔐 CHANGE PASSWORD
     * ------------------------------------------------------- */
    @Transactional
    public void changePassword(ChangePasswordRequestDTO dto) {

        User admin = getAuthenticatedUserEntity();

        // 1. Verify old password
        if (!passwordEncoder.matches(dto.getOldPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password.");
        }

        // 2. Prevent using the same password
        if (passwordEncoder.matches(dto.getNewPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as the old password.");
        }

        // 3. Update password
        admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepo.save(admin);

        activityLogService.logActivity(
                admin.getFullName() + " changed their password.",
                "Security"
        );
    }


    /* -------------------------------------------------------
     * 📷 UPDATE SCHOOL LOGO
     * ------------------------------------------------------- */
    @Transactional
    public SchoolProfileDTO updateSchoolLogo(MultipartFile file) {

        User admin = getAuthenticatedUserEntity();
        School school = admin.getSchool();

        if (school == null) {
            throw new EntityNotFoundException("No school associated with this admin.");
        }

        try {
            String logoUrl = fileStorageService.saveSchoolLogo(file, school.getId());

            school.setLogoUrl(logoUrl);
            School updated = schoolRepository.save(school);

            activityLogService.logActivity(
                    admin.getFullName() + " updated the school logo.",
                    "Settings"
            );

            return mapToDTO(updated);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save school logo.", e);
        }
    }
}
