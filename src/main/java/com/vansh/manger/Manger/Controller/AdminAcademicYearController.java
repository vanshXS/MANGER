package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.AcademicYearDTO;
import com.vansh.manger.Manger.Service.AdminAcademicYearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/academic-years")
@RequiredArgsConstructor
public class AdminAcademicYearController {

    private final AdminAcademicYearService academicYearService;

    /**
     * Creates a new Academic Year (e.g., "2025-2026").
     */
    @PostMapping
    public ResponseEntity<AcademicYearDTO> createAcademicYear(@Valid @RequestBody AcademicYearDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicYearService.createAcademicYear(dto));
    }

    /**
     * Gets a list of all academic years.
     */
    @GetMapping
    public ResponseEntity<List<AcademicYearDTO>> getAllAcademicYears() {
        return ResponseEntity.ok(academicYearService.getAllAcademicYears());
    }

    /**
     * Sets a specific academic year as the "current" one.
     */
    @PutMapping("/{yearId:\\d+}/set-current")
    public ResponseEntity<AcademicYearDTO> setCurrentAcademicYear(@PathVariable Long yearId) {
        return ResponseEntity.ok(academicYearService.setCurrentAcademicYear(yearId));
    }

    /**
     * Closes the current academic year (sets closed=true, isCurrent=false).
     * Required before promoting students from that year to the next.
     */
    @PostMapping("/close-current")
    public ResponseEntity<Void> closeCurrentAcademicYear() {
        academicYearService.closeAcademicYear();
        return ResponseEntity.ok().build();
    }
}
