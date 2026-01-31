package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.ClassroomResponseDTO;
import com.vansh.manger.Manger.DTO.PromotionContextDTO;
import com.vansh.manger.Manger.DTO.PromotionRequestDTO;
import com.vansh.manger.Manger.DTO.StudentPromotionDTO;
import com.vansh.manger.Manger.Service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promotion")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final PromotionService promotionService;

    /**
     * Returns promotion context: whether promotion is allowed, current/closed academic year info, and message for the UI.
     */
    @GetMapping("/context")
    public ResponseEntity<PromotionContextDTO> getPromotionContext() {
        return ResponseEntity.ok(promotionService.getPromotionContext());
    }

    @PostMapping("/classroom")
    public ResponseEntity<Void> promoteClassroom(@Valid @RequestBody PromotionRequestDTO request) {
        promotionService.promoteClassroom(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Promote / repeat / dropout / graduate a single student.
     * For PROMOTE or REPEAT, targetClassroomId is required; for DROPOUT and GRADUATE it is ignored.
     */
    @PostMapping("/student")
    public ResponseEntity<Void> promoteStudent(@Valid @RequestBody StudentPromotionDTO request) {
        promotionService.promoteSingleStudent(request);
        return ResponseEntity.ok().build();
    }

    // NEW: Get classrooms specifically for the closed year (Source Classrooms)
    @GetMapping("/classrooms/source")
    public ResponseEntity<List<ClassroomResponseDTO>> getSourceClassrooms() {
        // We get the context to find the closed year ID internally, or pass it
        PromotionContextDTO context = promotionService.getPromotionContext();
        if (context.getClosedAcademicYear() == null) {
            return ResponseEntity.ok(List.of()); // Return empty if no closed year
        }
        return ResponseEntity.ok(promotionService.getClassroomsForAcademicYear(context.getClosedAcademicYear().getId()));
    }

}
