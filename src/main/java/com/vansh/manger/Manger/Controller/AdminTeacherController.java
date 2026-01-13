package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.*;
import com.vansh.manger.Manger.Repository.TeacherRespository;
import com.vansh.manger.Manger.Service.AdminTeacherService;
import com.vansh.manger.Manger.Service.PDFService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
public class AdminTeacherController {

    private final AdminTeacherService adminTeacherService;
    private final PDFService pdfService;

   @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeacherResponseDTO> createTeacher(@Valid @ModelAttribute TeacherRequestDTO teacherRequestDTO, @RequestParam(value = "profilePicture", required = false )MultipartFile profilePic) {

       teacherRequestDTO.setProfilePicture(profilePic);
       TeacherResponseDTO teacher = adminTeacherService.createTeacher(teacherRequestDTO);

       return new ResponseEntity<>(teacher, HttpStatus.CREATED);
   }

   @GetMapping
    public ResponseEntity<List<TeacherResponseDTO>> getAllTeachers() {
       List<TeacherResponseDTO> teachers = adminTeacherService.getAllTeachers();

       return new ResponseEntity<>(teachers, HttpStatus.OK);
   }

   @GetMapping("/{teacherId:\\d+}")
    public ResponseEntity<?> getTeacherById(@PathVariable Long teacherId) {

       return ResponseEntity.ok(adminTeacherService.getTeacherById(teacherId));


   }

    @PutMapping(value = "/{teacherId:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeacherResponseDTO> updateTeacher(@PathVariable Long teacherId, @Valid @ModelAttribute TeacherRequestDTO teacherRequestDTO) {

       return new ResponseEntity<>(adminTeacherService.updateTeacher(teacherId, teacherRequestDTO), HttpStatus.OK);
    }

   @DeleteMapping("/{teacherId:\\d+}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long teacherId) {
        adminTeacherService.delete(teacherId);

        return ResponseEntity.ok("Deleted");
   }



    @GetMapping("/{teacherId:\\d+}/slip")
    public ResponseEntity<byte[]> generateTeacherSlip(@PathVariable Long teacherId) {
        TeacherResponseDTO teacher = adminTeacherService.getTeacherById(teacherId);
        byte[] pdfBytes = pdfService.generateTeacherSlip(teacher);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                        teacher.getFirstName() + "_" + teacher.getLastName() + "_slip.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
