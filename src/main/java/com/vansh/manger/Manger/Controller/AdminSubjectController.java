package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.SubjectRequestDTO;
import com.vansh.manger.Manger.DTO.SubjectResponseDTO;
import com.vansh.manger.Manger.Entity.Subject;
import com.vansh.manger.Manger.Service.AdminSubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/subjects")
@RequiredArgsConstructor
public class AdminSubjectController {

    private final AdminSubjectService adminSubjectService;

    @PostMapping
    public ResponseEntity<SubjectResponseDTO> createSubject(@Valid @RequestBody SubjectRequestDTO subjectRequestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(adminSubjectService.createSubject(subjectRequestDTO));
    }
    @GetMapping
    public ResponseEntity<List<SubjectResponseDTO>> getAllSubjects() {
        return ResponseEntity.ok(adminSubjectService.getAllSubjects());
    }

    @DeleteMapping("/{subjectId:\\d+}")
    public ResponseEntity<Void> deleteSubject(@PathVariable("subjectId") Long subjectId) {
        adminSubjectService.deleteSubject(subjectId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{subjectId:\\d+}")
    public ResponseEntity<SubjectResponseDTO> updateStudent(
            @PathVariable("subjectId") Long subjectId,
            @Valid @RequestBody SubjectRequestDTO subjectRequestDTO
    ) {
        return ResponseEntity.ok(adminSubjectService.updateSubject(subjectId, subjectRequestDTO));
    }






}