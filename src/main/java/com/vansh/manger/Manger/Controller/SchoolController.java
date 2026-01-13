package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.SchoolRegistrationRequestDTO;
import com.vansh.manger.Manger.Entity.School;
import com.vansh.manger.Manger.Service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schools")
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<School> registerSchool(@Valid @ModelAttribute SchoolRegistrationRequestDTO dto) {

        return ResponseEntity.ok(schoolService.registerSchool(dto));
    }


}
