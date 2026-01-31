package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.Entity.ActivityLog;
import com.vansh.manger.Manger.Entity.School;
import com.vansh.manger.Manger.Repository.ActivityLogRepository;
import com.vansh.manger.Manger.util.AdminSchoolConfig;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final AdminSchoolConfig schoolConfig;

    @Transactional
    public void logActivity(String description, String category) {
        School school = schoolConfig.requireCurrentSchool();
        ActivityLog log = ActivityLog.builder()
                .description(description)
                .category(category)
                .school(school)
                .build();

        activityLogRepository.save(log);
    }

    @Transactional
    public void logActivityForSchool(School school, String description, String category) {
       ActivityLog log = ActivityLog.builder()
                       .school(school)
                               .category(category)
                                       .description(description)
                                               .build();



        activityLogRepository.save(log);
    }
}
