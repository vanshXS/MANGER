//package com.vansh.manger.Manger.Service;
//
//import com.vansh.manger.Manger.DTO.MarksRequestDTO;
//import com.vansh.manger.Manger.DTO.MarksResponseDTO;
//import com.vansh.manger.Manger.Entity.Student;
//import com.vansh.manger.Manger.Entity.StudentSubject;
//import com.vansh.manger.Manger.Entity.Subject;
//import com.vansh.manger.Manger.Repository.StudentRepository;
//import com.vansh.manger.Manger.Repository.StudentSubjectsRepository;
//import com.vansh.manger.Manger.Repository.SubjectRepository;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class MarkService {
//
//    private final StudentSubjectsRepository studentSubjectsRepository;
//    private final StudentRepository studentRepository;
//    private final SubjectRepository subjectRepository;
//    private final PDFService pdfService;
//    private final EmailService emailService;
//
//    private final ModelMapper modelMapper;
//    @Transactional
//    public MarksResponseDTO addOrUpdateMarks(MarksRequestDTO dto) {
//
//        Student student = studentRepository.findById(dto.getStudentId())
//                .orElseThrow(() -> new RuntimeException("Student not found"));
//
//        Subject subject = subjectRepository.findById(dto.getSubjectId())
//                .orElseThrow(() -> new RuntimeException("Subject not found"));
//
//        StudentSubject record = studentSubjectsRepository.findByStudentAndSubjectAndExamName(student, subject, dto.getExamName())
//                .orElse(StudentSubject.builder()
//                        .student(student)
//                        .subject(subject)
//                        .examName(dto.getExamName())
//                        .totalMarks(100.0)
//                        .build());
//
//        // Defensive null check
//        Double totalMarks = dto.getTotalMarks() != null ? dto.getTotalMarks() : record.getTotalMarks();
//        if (totalMarks == null) {
//            throw new IllegalArgumentException("Total marks must be provided.");
//        }
//
//        if (dto.getMarksObtained() > totalMarks) {
//            throw new IllegalArgumentException("Marks obtained cannot be greater than total marks.");
//        }
//
//        record.setMarksObtained(dto.getMarksObtained());
//        record.setTotalMarks(totalMarks);
//        record.setGrade(calculateGrade(dto.getMarksObtained(), totalMarks));
//
//        StudentSubject saved = studentSubjectsRepository.save(record);
//
//
//        return convertToResponse(saved);
//    }
//
//    public void senMarksheetToStudent(Long studentId, String examName) {
//        Student student = studentRepository.findById(studentId)
//                .orElseThrow(() -> new RuntimeException("Student not found"));
//
//        List<StudentSubject> allSubjects = studentSubjectsRepository.findByStudentAndExamName(student, examName);
//
//        if(allSubjects.isEmpty()) {
//            throw new RuntimeException("No marks found for student in this exam.");
//        }
//
//        byte[] pdfBytes = pdfService.generateMarksSheet(student, allSubjects, examName);
//
//        emailService.sendMarksheet(
//                student.getEmail(),
//                pdfBytes,
//                student.getFirstName(),
//                examName,
//                student.getRollNo(),
//                null
//        );
//    }
//
//    private String calculateGrade(Double marks, Double totalMarks) {
//         double percentage = (marks / totalMarks) * 100;
//
//         if(percentage >= 90) return "A+";
//         else if(percentage >= 80) return "A";
//         else if(percentage >= 70) return "B";
//         else if(percentage >= 60) return "C";
//         else if(percentage >= 50) return "D";
//         else if(percentage >= 40) return "E";
//         else return "F";
//    }
//
//    public List<MarksResponseDTO> getMarksByStudent(Long studentId, String examName) {
//
//        Student student = studentRepository.findById(studentId)
//                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
//
//        return studentSubjectsRepository.findByStudentAndExamName(student, examName)
//                .stream()
//                .map(this :: convertToResponse)
//                .toList();
//    }
//
//    private MarksResponseDTO convertToResponse(StudentSubject record) {
//
//        double percentage = (record.getMarksObtained() / record.getTotalMarks()) * 100;
//
//        return MarksResponseDTO.builder()
//                .id(record.getId())
//                .studentName(record.getStudent().getFirstName() + " " + record.getStudent().getLastName())
//                .subjectName(record.getSubject().getName())
//                .grade(record.getGrade())
//                .examName(record.getExamName())
//
//                .marksObtained(record.getMarksObtained())
//                .totalMarks(record.getTotalMarks())
//                .percentage(percentage)
//                .build();
//    }
//}
