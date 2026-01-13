package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.ActivityLogDTO;
import com.vansh.manger.Manger.DTO.ClassroomEnrollmentDTO;
import com.vansh.manger.Manger.DTO.DashboardKpiDTO;
import com.vansh.manger.Manger.DTO.TeacherWorkloadDTO;
import com.vansh.manger.Manger.Service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/kpis")
    public ResponseEntity<DashboardKpiDTO> getKpis() {
        return ResponseEntity.ok(adminDashboardService.getKpis());
    }

    @GetMapping("/enrollment-overview")
    public ResponseEntity<List<ClassroomEnrollmentDTO>> getEnrollmentOverview() {

        return ResponseEntity.ok(adminDashboardService.getEnrollmentOverview());
    }

    @GetMapping("/teacher-workload")
    public ResponseEntity<List<TeacherWorkloadDTO>> getTeacherWorkload() {
        return ResponseEntity.ok(adminDashboardService.getTeacherWorkload());
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<ActivityLogDTO>> getRecentActivity() {
        return ResponseEntity.ok(adminDashboardService.getRecentActivity());
    }

    @GetMapping("/activity-logs")
    public ResponseEntity<?> getAllActivityLog(Pageable pageable) {

        return ResponseEntity.ok(adminDashboardService.getAllActivityLogs(pageable));
    }

}
