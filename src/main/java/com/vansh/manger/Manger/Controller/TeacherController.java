//package com.vansh.manger.Manger.Controller;
//
//import com.vansh.manger.Manger.DTO.*;
//import com.vansh.manger.Manger.Entity.User;
//import com.vansh.manger.Manger.Service.AttendanceService;
//import com.vansh.manger.Manger.Service.CustomUserDetailService;
//import com.vansh.manger.Manger.Service.MarkService;
//import com.vansh.manger.Manger.Service.TeacherService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.coyote.Response;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/teachers")
//@Slf4j
//public class TeacherController {
//
//    private final AttendanceService attendanceService;
//    private final MarkService markService;
//    private final TeacherService teacherService;
//
//
//    //mark single attendance
//    @PostMapping("/attendance/mark")
//    public ResponseEntity<AttendanceResponseDTO> markSingle(@RequestBody StudentAttendanceDTO request) {
//        AttendanceResponseDTO dto = attendanceService.markAttendance(
//                request.getStudentSubjectId(),
//                java.time.LocalDate.now(),
//                request.isPresent()
//        );
//
//        return new ResponseEntity<>(dto, HttpStatus.OK);
//
//    }
//
//    //bulk attendance
//    @PostMapping("/attendance/mark/bulk")
//    public ResponseEntity<List<AttendanceResponseDTO>> markBulk(@RequestBody BulkAttendanceRequest request) {
//        List<AttendanceResponseDTO> saved = attendanceService.markBulk(request);
//
//        return new ResponseEntity<>(saved, HttpStatus.OK);
//    }
//
//    //get attendance for a studentSubject
//    @GetMapping("/attendance/{studentSubjectId}")
//    public ResponseEntity<List<AttendanceResponseDTO>> getAttendance(@PathVariable Long studentSubjectId) {
//        return
//                ResponseEntity.ok(attendanceService.getAttendanceForStudentSubject(studentSubjectId));
//    }
//
//    //attendance percentage
//    @GetMapping("/attendance/percentage/{studentSubjectId}")
//    public ResponseEntity<Double> getAttendancePercentage(@PathVariable Long studentSubjectId) {
//        return ResponseEntity.ok(attendanceService.calculateAttendancePercentage(studentSubjectId));
//    }
//
//    //MARKS ENDPOINTS
//    @PostMapping("/marks")
//    public ResponseEntity<MarksResponseDTO> addOrUpdateMark(@Valid @RequestBody MarksRequestDTO dto) {
//        return ResponseEntity.ok(markService.addOrUpdateMarks(dto));
//    }
//
//    @PostMapping("/marks/{studentId}/exam/{examName}")
//    public ResponseEntity<?> sendMarkSheetToStudent(@PathVariable Long studentId, @PathVariable String examName) {
//
//       markService.senMarksheetToStudent(studentId, examName);
//
//       return ResponseEntity.ok("Send successfully");
//    }
//
//    @GetMapping("/marks/student/{studentId}")
//    public ResponseEntity<List<MarksResponseDTO>> getMarksByStudent(
//            @PathVariable Long studentId,
//            @RequestParam String examName
//    ) {
//        return ResponseEntity.ok(markService.getMarksByStudent(studentId, examName));
//    }
//
//    //get my classroom
//    @GetMapping("/myclasses")
//    public ResponseEntity<List<ClassroomResponseDTO>> getMyClasses(@AuthenticationPrincipal User user) {
//
//        String email = user.getEmail();
//        log.info("Email is " + email);
//
//        log.info(teacherService.getMyClasses(email).toString());
//        return ResponseEntity.ok(teacherService.getMyClasses(email));
//
//
//    }
//
//    @GetMapping("/mysubjects")
//    public ResponseEntity<List<SubjectResponseDTO>> getMySubjects(@AuthenticationPrincipal User user) {
//
//        String email = user.getEmail();
//
//        return ResponseEntity.ok(teacherService.getMySubject(email));
//    }
//
//
//
//
//
//
//
//}
