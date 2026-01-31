package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.AcademicYearDTO;
import com.vansh.manger.Manger.Entity.AcademicYear;
import com.vansh.manger.Manger.Entity.School;
import com.vansh.manger.Manger.Repository.AcademicYearRepository;
import com.vansh.manger.Manger.util.AdminSchoolConfig;
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
    private final AdminSchoolConfig adminSchoolConfig;

    public AcademicYearDTO mapToResponse(AcademicYear academicYear) {
        return AcademicYearDTO.builder()
                .id(academicYear.getId())
                .name(academicYear.getName())
                .startDate(academicYear.getStartDate())
                .endDate(academicYear.getEndDate())
                .isCurrent(Boolean.TRUE.equals(academicYear.getIsCurrent()))
                .closed(academicYear.getClosed())
                .build();
    }

    @Transactional
    public AcademicYearDTO createAcademicYear(AcademicYearDTO dto) {
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
        String name = dto.getName() != null ? dto.getName().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Year name is required.");
        }
        Long schoolId = adminSchoolConfig.requireCurrentSchool().getId();
        if (academicYearRepository.existsBySchool_IdAndName(schoolId, name)) {
            throw new IllegalArgumentException("An academic year with name \"" + name + "\" already exists for this school.");
        }
        AcademicYear newYear = AcademicYear.builder()
                .name(name)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isCurrent(false)
                .closed(true)
                .school(adminSchoolConfig.requireCurrentSchool())
                .build();

        AcademicYear savedYear = academicYearRepository.save(newYear);
        activityLogService.logActivity("Created new academic year: " + savedYear.getName(), "Settings");

        return mapToResponse(savedYear);
    }

    public List<AcademicYearDTO> getAllAcademicYears() {
        List<AcademicYear> years = academicYearRepository.findBySchool_IdOrderByStartDateDesc(
                adminSchoolConfig.requireCurrentSchool().getId());

        return years.stream()
                .map(year -> {
                    log.debug("Academic Year: id={}, name={}, isCurrent={}",
                            year.getId(), year.getName(), year.getIsCurrent());
                    return mapToResponse(year);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public AcademicYearDTO setCurrentAcademicYear(Long yearId) {

        // Find the year to set as current
        AcademicYear newCurrentYear = academicYearRepository.findByIdAndSchool_Id(yearId, adminSchoolConfig.requireCurrentSchool().getId())
                .orElseThrow(() -> new EntityNotFoundException("Academic year not found with id: " + yearId));

        if (newCurrentYear.getIsCurrent()) {

            return mapToResponse(newCurrentYear);
        }

        // Step 1: Unset ALL current years using bulk update
        int updatedCount = academicYearRepository.unsetAllCurrentYearsBySchool(adminSchoolConfig.requireCurrentSchool().getId());


        entityManager.flush();
        entityManager.clear();

        // Step 2: Reload the year and set it as current
        newCurrentYear = academicYearRepository.findByIdAndSchool_Id(yearId, adminSchoolConfig.requireCurrentSchool().getId())
                .orElseThrow(() -> new EntityNotFoundException("Academic year not found with id: " + yearId));

        newCurrentYear.setIsCurrent(true);

        AcademicYear savedYear = academicYearRepository.saveAndFlush(newCurrentYear);



        // Step 3: Verify the change
        entityManager.clear(); // Clear cache again
        AcademicYear verifiedYear = academicYearRepository.findByIdAndSchool_Id(yearId, adminSchoolConfig.requireCurrentSchool().getId())
                .orElseThrow(() -> new EntityNotFoundException("Academic year not found after save"));



        if (!verifiedYear.getIsCurrent()) {

            throw new RuntimeException("Failed to set academic year as current");
        }

        activityLogService.logActivity("Set current academic year to: " + savedYear.getName(), "Settings");


        return mapToResponse(verifiedYear);
    }

    @Transactional
    public void closeAcademicYear() {
        School school = adminSchoolConfig.requireCurrentSchool();

        AcademicYear currentYear =
                academicYearRepository.findByIsCurrentAndSchool_Id(true, school.getId())
                        .orElseThrow(() -> new RuntimeException("No active academic year"));

        currentYear.setClosed(true);
        currentYear.setIsCurrent(false);

        academicYearRepository.save(currentYear);

        activityLogService.logActivity(
                "Academic year closed: " +
                        currentYear.getName(), "Academic Year"
        );

    }
}
