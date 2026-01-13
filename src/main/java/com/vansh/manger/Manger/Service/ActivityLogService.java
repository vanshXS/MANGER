package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.Entity.ActivityLog;
import com.vansh.manger.Manger.Repository.ActivityLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void logActivity(String description, String category) {
        ActivityLog log = ActivityLog.builder()
                .description(description)
                .category(category)
                .build();

        activityLogRepository.save(log);
    }
}
