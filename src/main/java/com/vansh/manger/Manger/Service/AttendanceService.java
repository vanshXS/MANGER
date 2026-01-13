//package com.vansh.manger.Manger.Service;
//
//import com.vansh.manger.Manger.DTO.AttendanceResponseDTO;
//import com.vansh.manger.Manger.DTO.BulkAttendanceRequest;
//import com.vansh.manger.Manger.DTO.StudentAttendanceDTO;
//import com.vansh.manger.Manger.Entity.Attendance;
//import com.vansh.manger.Manger.Entity.StudentSubject;
//import com.vansh.manger.Manger.Repository.AttendanceRepository;
//import com.vansh.manger.Manger.Repository.StudentSubjectsRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class AttendanceService {
//    private final StudentSubjectsRepository studentSubjectsRepository;
//    private final AttendanceRepository attendanceRepository;
//
//    @Transactional
//    public AttendanceResponseDTO markAttendance(Long studentSubjectId, LocalDate date, boolean present) {
//
//        StudentSubject ss = studentSubjectsRepository.findById(studentSubjectId)
//                .orElseThrow(() -> new RuntimeException("Student subject not found"));
//
//        boolean exists = attendanceRepository.existsByStudentSubjectAndLocalDate(ss,date);
//        if (exists) {
//            throw new IllegalStateException("Attendance already marked for studentSubjectId " + studentSubjectId + " on " + date);
//        }
//        Attendance attendance = Attendance.builder()
//                .studentSubject(ss)
//                .present(present)
//                .localDate(date)
//                .build();
//        Attendance saved = attendanceRepository.save(attendance);
//
//        return new AttendanceResponseDTO(saved.getId(), saved.getStudentSubject().getId(),saved.getLocalDate(), saved.isPresent());
//
//    }
//    @Transactional
//    public List<AttendanceResponseDTO> markBulk(BulkAttendanceRequest request) {
//        LocalDate date = request.getDate();
//        List<StudentAttendanceDTO> records = request.getRecords();
//
//        if(request.getClassroomId() != null && (records == null || records.isEmpty())) {
//
//            List<StudentSubject> byClassroom = studentSubjectsRepository.findByStudent_ClassroomId(request.getClassroomId());
//            records = byClassroom.stream()
//                    .map(ss -> {
//                        StudentAttendanceDTO dto = new StudentAttendanceDTO();
//                        dto.setStudentSubjectId(ss.getId());
//                        dto.setPresent(false);
//
//                        return dto;
//                    }).toList();
//        }
//
//        if(records == null || records.isEmpty()) return Collections.emptyList();
//
//        // deduplicate studentSubjectIds in request
//        Map<Long, Boolean> unique = new LinkedHashMap<>();
//
//        for(StudentAttendanceDTO r : records) {
//            unique.put(r.getStudentSubjectId(), r.isPresent());
//        }
//
//        List<AttendanceResponseDTO> savedResponse = new ArrayList<>();
//
//        for(Map.Entry<Long, Boolean> e : unique.entrySet()) {
//            Long ssId = e.getKey();
//            boolean present = e.getValue();
//            StudentSubject ss =
//                    studentSubjectsRepository.findById(ssId)
//                            .orElseThrow( () -> new NoSuchElementException("StudentSubject not found: " + ssId));
//
//            //skip already if already exist for this date
//            if(attendanceRepository.existsByStudentSubjectAndLocalDate(ss, date)) continue;
//
//            Attendance a = new Attendance();
//            a.setStudentSubject(ss);
//            a.setLocalDate(date);
//            a.setPresent(present);
//            Attendance saved = attendanceRepository.save(a);
//
//            savedResponse.add(new AttendanceResponseDTO(saved.getId(), ss.getId(), saved.getLocalDate(), saved.isPresent()));
//        }
//
//        return savedResponse;
//    }
//
//    public List<AttendanceResponseDTO> getAttendanceForStudentSubject(Long studentSubjectId) {
//        List<Attendance> records = attendanceRepository.findByStudentSubjectId(studentSubjectId);
//
//        return records.stream()
//                .map(a -> new AttendanceResponseDTO(a.getId(), a.getStudentSubject().getId(), a.getLocalDate(), a.isPresent()))
//                .collect(Collectors.toList());
//
//    }
//
//    public double calculateAttendancePercentage(Long studentSubjectId) {
//        List<Attendance> records = attendanceRepository.findByStudentSubjectId(studentSubjectId);
//
//        if(records.isEmpty()) return 0.0;
//
//        long presentCount = records.stream().filter(Attendance :: isPresent).count();
//        return (presentCount * 100.00) / records.size();
//    }
//}
