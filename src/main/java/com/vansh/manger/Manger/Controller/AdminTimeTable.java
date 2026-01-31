package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.TimeTableRequestDTO;
import com.vansh.manger.Manger.DTO.TimeTableResponseDTO;
import com.vansh.manger.Manger.Entity.TimeTable;
import com.vansh.manger.Manger.Service.TimeTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/timetable")
@RequiredArgsConstructor
public class AdminTimeTable {

    private final TimeTableService timeTableService;

    @PostMapping
    public ResponseEntity<TimeTableResponseDTO> createTimeTable(@RequestBody @Valid TimeTableRequestDTO dto) {

        return ResponseEntity.ok(timeTableService.createTimeTable(dto));

    }

    @GetMapping("/teacher/{teacherId:\\d+}")
    public ResponseEntity<List<TimeTableResponseDTO>> getTimeTableByTeacherId(@PathVariable Long teacherId) {

        return ResponseEntity.ok(timeTableService.getByTeacherId(teacherId));
    }

    @GetMapping("/classroom/{classroomId:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<TimeTableResponseDTO>> getTimeTableByClassroom(@PathVariable Long classroomId) {
        return ResponseEntity.ok(timeTableService.getByClassroomId(classroomId));
    }

    @PutMapping("/{timeTableId:\\d+}")
    public ResponseEntity<TimeTableResponseDTO> update(@PathVariable Long timeTableId, @Valid @RequestBody TimeTableRequestDTO dto) {

        return ResponseEntity.ok(timeTableService.updateTimeTable(timeTableId, dto));
    }
    @DeleteMapping("/{timeTableId:\\d+}")
    public void delete(@PathVariable Long timeTableId) {

        timeTableService.deleteTimeTable(timeTableId);

    }
}
