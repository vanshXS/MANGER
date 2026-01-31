package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.ChangePasswordRequestDTO;
import com.vansh.manger.Manger.DTO.SchoolProfileDTO;
import com.vansh.manger.Manger.Service.AdminSchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/school")
@RequiredArgsConstructor
public class AdminSchoolController {

    private final AdminSchoolService adminSchoolService;

    /**
     * GET endpoint to fetch the logged-in admin's school profile.
     */
    @GetMapping
    public ResponseEntity<SchoolProfileDTO> getSchoolProfile() {
        // The service automatically handles getting the *correct* school
        // based on the admin's JWT.
        return ResponseEntity.ok(adminSchoolService.getSchoolProfile());
    }

    /**
     * PUT endpoint to update the logged-in admin's school profile.
     */
    @PutMapping
    public ResponseEntity<SchoolProfileDTO> updateSchoolProfile(
            @Valid @RequestBody SchoolProfileDTO profileDTO) {
        // The service automatically finds and updates the correct school.
        return ResponseEntity.ok(adminSchoolService.updateSchoolProfile(profileDTO));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDTO changePasswordRequestDTO) {

        adminSchoolService.changePassword(changePasswordRequestDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SchoolProfileDTO> updateSchoolLogo(
            @RequestParam("file") MultipartFile logoFile) {

        // The service method will find the admin's school and update its logo
        SchoolProfileDTO updatedProfile = adminSchoolService.updateSchoolLogo(logoFile);
        return ResponseEntity.ok(updatedProfile);
    }


}