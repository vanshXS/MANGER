package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.TimeTableResponseDTO;
import com.vansh.manger.Manger.Service.TimeTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/timetable")
@RequiredArgsConstructor
public class TeacherTimeTable {

    private final TimeTableService timeTableService;

    @GetMapping
    public ResponseEntity<List<TimeTableResponseDTO>> getMyTimeTable() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(timeTableService.getMyTimeTable(email));
    }

}
