package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.AcademicYearDTO;
import com.vansh.manger.Manger.Entity.AcademicYear;
import com.vansh.manger.Manger.Repository.AcademicYearRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final ActivityLogService activityLogService;
    private final EntityManager entityManager;

    public AcademicYearDTO mapToResponse(AcademicYear academicYear) {
        return AcademicYearDTO.builder()
                .id(academicYear.getId())
                .name(academicYear.getName())
                .startDate(academicYear.getStartDate())
                .endDate(academicYear.getEndDate())
                .isCurrent(academicYear.isCurrent())
                .build();
    }

    @Transactional
    public AcademicYearDTO createAcademicYear(AcademicYearDTO dto) {
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        AcademicYear newYear = AcademicYear.builder()
                .name(dto.getName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isCurrent(false)
                .build();

        AcademicYear savedYear = academicYearRepository.save(newYear);
        activityLogService.logActivity("Created new academic year: " + savedYear.getName(), "Settings");

        return mapToResponse(savedYear);
    }

    public List<AcademicYearDTO> getAllAcademicYears() {
        List<AcademicYear> years = academicYearRepository.findAll();
        log.info("Fetched {} academic years from database", years.size());

        return years.stream()
                .map(year -> {
                    log.debug("Academic Year: id={}, name={}, isCurrent={}",
                            year.getId(), year.getName(), year.isCurrent());
                    return mapToResponse(year);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public AcademicYearDTO setCurrentAcademicYear(Long yearId) {

        // Find the year to set as current
        AcademicYear newCurrentYear = academicYearRepository.findById(yearId)
                .orElseThrow(() -> new EntityNotFoundException("Academic year not found with id: " + yearId));



        if (newCurrentYear.isCurrent()) {

            return mapToResponse(newCurrentYear);
        }

        // Step 1: Unset ALL current years using bulk update
        int updatedCount = academicYearRepository.unsetAllCurrentYears();



        entityManager.flush();
        entityManager.clear();

        // Step 2: Reload the year and set it as current
        newCurrentYear = academicYearRepository.findById(yearId)
                .orElseThrow(() -> new EntityNotFoundException("Academic year not found with id: " + yearId));

        log.info("Setting academic year as current: id={}, name={}", newCurrentYear.getId(), newCurrentYear.getName());
        newCurrentYear.setCurrent(true);

        AcademicYear savedYear = academicYearRepository.saveAndFlush(newCurrentYear);

        log.info("Saved academic year: id={}, name={}, isCurrent={}",
                savedYear.getId(), savedYear.getName(), savedYear.isCurrent());

        // Step 3: Verify the change
        entityManager.clear(); // Clear cache again
        AcademicYear verifiedYear = academicYearRepository.findById(yearId)
                .orElseThrow(() -> new EntityNotFoundException("Academic year not found after save"));



        if (!verifiedYear.isCurrent()) {

            throw new RuntimeException("Failed to set academic year as current");
        }

        activityLogService.logActivity("Set current academic year to: " + savedYear.getName(), "Settings");


        return mapToResponse(verifiedYear);
    }
}