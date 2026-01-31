package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.AssignmentRequestDTO;
import com.vansh.manger.Manger.DTO.AssignmentResponseDTO;
import com.vansh.manger.Manger.DTO.UpdateAssignmentMandatoryDTO;
import com.vansh.manger.Manger.DTO.UpdateAssignmentTeacherDTO;
import com.vansh.manger.Manger.Service.AdminAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.spel.ast.Assign;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/assignments")
@RequiredArgsConstructor
public class AdminAssignmentController {

    private final AdminAssignmentService adminAssignmentService;

    @PostMapping
    public ResponseEntity<AssignmentResponseDTO> createAssignment(@Valid @RequestBody AssignmentRequestDTO assignmentRequestDTO) {
        return new ResponseEntity<>(adminAssignmentService.createAssignment(assignmentRequestDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AssignmentResponseDTO>> getAllAssignments() {
        return new ResponseEntity<>(adminAssignmentService.getAllAssignments(), HttpStatus.OK);
    }
    @GetMapping("/by-classroom/{classroomId:\\d+}")
    public ResponseEntity<List<AssignmentResponseDTO>> getAssignmentsByClassroom(
            @PathVariable Long classroomId
    ) {
        return ResponseEntity.ok(adminAssignmentService.getAssignmentsByClassroom(classroomId));
    }

    @PutMapping("/{assignmentId:\\d+}/teacher")
    public ResponseEntity<AssignmentResponseDTO> updateAssignmentTeacher(@PathVariable Long assignmentId, @Valid @RequestBody UpdateAssignmentTeacherDTO dto) {
        return ResponseEntity.ok(adminAssignmentService.updateAssignmentTeacher(assignmentId, dto.getTeacherId()));
    }

    @DeleteMapping("/{assignmentId:\\d+}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId) {
        adminAssignmentService.deleteAssignment(assignmentId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{teacherId:\\d+}/teacher")
    public ResponseEntity<AssignmentResponseDTO> removeTeacherFromAssignment(@PathVariable Long teacherId) {
        AssignmentResponseDTO response = adminAssignmentService.unassignedTeacher(teacherId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{assignmentId:\\d+}/mandatory")
    public ResponseEntity<AssignmentResponseDTO> updateMandatory(
            @PathVariable Long assignmentId,
            @RequestBody UpdateAssignmentMandatoryDTO dto
    ) {
        return ResponseEntity.ok(
                adminAssignmentService.updateMandatorySubject(assignmentId, dto.isMandatory())
        );
    }


}
