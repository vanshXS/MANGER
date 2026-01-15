package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.StudentRequestDTO;
import com.vansh.manger.Manger.DTO.StudentResponseDTO;
import com.vansh.manger.Manger.DTO.SubjectResponseDTO;

import com.vansh.manger.Manger.Service.AdminStudentService;
import com.vansh.manger.Manger.Service.AdminSubjectService;
import com.vansh.manger.Manger.Service.PDFService;
import jakarta.mail.Multipart;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final AdminStudentService adminStudentService;
    private final PDFService pdfService;
    private final AdminSubjectService adminSubjectService;

    /**
     * Creates a new student (Admission).
     * This now includes creating their first Enrollment record and sending a welcome email.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResponseDTO> createStudent(@ModelAttribute @Valid StudentRequestDTO studentRequestDTO, @RequestParam(value = "profilePicture", required = false)MultipartFile profilePic) throws IOException {
        // This response DTO contains the one-time raw password for the admin
        studentRequestDTO.setProfilePicture(profilePic);
        StudentResponseDTO studentResponseDTO = adminStudentService.createStudent(studentRequestDTO);
        return new ResponseEntity<>(studentResponseDTO, HttpStatus.CREATED);
    }

    /**
     * Gets a paginated list of all students and their *current* enrollment status.
     */
    @GetMapping
    public ResponseEntity<Page<StudentResponseDTO>> getAllStudents(
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable
    ) {

        return ResponseEntity.ok(adminStudentService.getAllStudents(pageable));
    }

    /**
     * Gets a single student's profile by their permanent ID.
     */
    @GetMapping("/{studentId:\\d+}")
    public ResponseEntity<StudentResponseDTO> getStudentById(@PathVariable Long studentId) {
        StudentResponseDTO studentResponseDTO = adminStudentService.getStudentById(studentId);
        return new ResponseEntity<>(studentResponseDTO, HttpStatus.OK);
    }

    /**
     * Updates a student's core profile information (name, phone, picture).
     * This does NOT change their enrollment.
     */
    @PutMapping(value = "/{studentId:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResponseDTO> updateStudent(@PathVariable Long studentId,
                                                            @ModelAttribute @Valid StudentRequestDTO studentRequestDTO,
                                                            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) throws IOException {

        studentRequestDTO.setProfilePicture(profilePicture);

        StudentResponseDTO updatedStudent = adminStudentService.updateStudent(studentId, studentRequestDTO);
        return new ResponseEntity<>(updatedStudent, HttpStatus.OK);
    }

    /**
     * Deletes a student and ALL their associated data (User, Enrollments, Subject links).
     */
    @DeleteMapping("/{studentId:\\d+}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long studentId) {
        adminStudentService.deleteById(studentId);
        return ResponseEntity.ok("Student deleted successfully");
    }

    /**
     * Gets a list of students *currently* enrolled in a specific classroom.
     */
    @GetMapping("/by-classroom/{classroomId:\\d+}")
    public ResponseEntity<List<StudentResponseDTO>> getStudentsByClassroom(@PathVariable Long classroomId) {
        return new ResponseEntity<>(adminStudentService.getStudentsByClassroom(classroomId), HttpStatus.OK);
    }

    /**
     * Assigns/Transfers a student to a different classroom in the *current* academic year.
     */
    @PostMapping("/{studentId:\\d+}/assign-classroom/{classroomId:\\d+}")
    public ResponseEntity<StudentResponseDTO> assignStudentToClassroom(
            @PathVariable Long studentId,
            @PathVariable Long classroomId
    ) {
        return new ResponseEntity<>(adminStudentService.assignStudentToClassroom(studentId, classroomId), HttpStatus.OK);
    }

    /**
     * Assigns an elective subject to a student.
     */
    @PostMapping("/{studentId:\\d+}/assign-subject/{subjectId:\\d+}")
    public ResponseEntity<StudentResponseDTO> assignStudentToSubject(
            @PathVariable Long studentId,
            @PathVariable Long subjectId
    ) {
        return new ResponseEntity<>(adminStudentService.assignStudentToSubject(studentId, subjectId), HttpStatus.OK);
    }

    /**
     * Get all subjects by the classroom Id
     */

    @GetMapping("/{classroomId:\\d+}/subjects")
    public ResponseEntity<List<SubjectResponseDTO>> getSubjectsByClassroom(@PathVariable Long classroomId) {
        return ResponseEntity.ok(adminSubjectService.subjectsByClassroomId(classroomId));
    }

    /**
     * Removes an elective subject from a student.
     */
    @DeleteMapping("/{studentId:\\d+}/remove-subject/{subjectId:\\d+}")
    public ResponseEntity<String> removeSubjectFromStudent(
            @PathVariable Long studentId,
            @PathVariable Long subjectId
    ) {
        adminStudentService.removeSubjectFromStudent(studentId, subjectId);
        return new ResponseEntity<>("Subject Removed", HttpStatus.OK);
    }

    /**
     * Gets the list of elective subjects for a single student.
     */
    @GetMapping("/subjects/{studentId:\\d+}")
    public ResponseEntity<List<SubjectResponseDTO>> getSubjectsOfStudents(@PathVariable Long studentId) {
        return new ResponseEntity<>(adminStudentService.getSubjectsOfStudent(studentId), HttpStatus.OK);
    }

    /**
     * Downloads a PDF slip for the student (password is NOT included).
     */
    @GetMapping("/{studentId:\\d+}/slip")
    public ResponseEntity<byte[]> downloadStudentSlip(@PathVariable Long studentId) {
        StudentResponseDTO studentResponseDTO = adminStudentService.getStudentById(studentId);
        byte[] pdfBytes = pdfService.generateStudentSlip(studentResponseDTO);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-slip-" + studentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }


    @PostMapping("/{studentId:\\d+}/send-password-reset")
    public ResponseEntity<String> sendPasswordResetEmail(@PathVariable Long studentId) {
        adminStudentService.sendPasswordReset(studentId);
        return ResponseEntity.ok("Password reset email sent successfully to the student.");
    }
}