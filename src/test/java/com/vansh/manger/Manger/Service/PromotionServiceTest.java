package com.vansh.manger.Manger.Service;

import com.vansh.manger.Manger.DTO.PromotionRequestDTO;
import com.vansh.manger.Manger.Entity.*;
import com.vansh.manger.Manger.Repository.*;
import com.vansh.manger.Manger.util.AdminSchoolConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import  org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PromotionServiceTest {

    @Mock
    private  EnrollmentRepository enrollmentRepository;
    @Mock
    private  ClassroomRespository classroomRespository;
    @Mock
    private AcademicYearRepository academicYearRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private AdminSchoolConfig schoolConfig;
    @Mock
    private AdminStudentService adminStudentService;
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private PromotionService promotionService;

    private School school;
    private AcademicYear oldYear;
    private AcademicYear newYear;
    private Classroom oldClassroom;
    private Classroom newClassroom;
    private Student student;
    private Enrollment oldEnrollment;



   @BeforeEach
    void setUp() {
       MockitoAnnotations.openMocks(this);
       school = School.builder().id(1L).name("Test School").build();
       oldYear = AcademicYear.builder().id(1L).isCurrent(false).closed(true).school(school).build();
       newYear = AcademicYear.builder().id(2L).isCurrent(true).school(school).build();
       oldClassroom = Classroom.builder().id(1L).name("Class A").school(school).build();
       newClassroom = Classroom.builder().id(2L).name("Class B").school(school).build();
       student = Student.builder().id(1L).firstName("Test").lastName("Student").school(school).build();

       oldEnrollment = Enrollment.builder()
               .id(1L)
               .student(student)
               .classroom(oldClassroom)
               .academicYear(oldYear)
               .status(StudentStatus.ACTIVE)
               .school(school)
               .build();
   }

   @Test
    void testPromoteClassroom_success() {
       PromotionRequestDTO dto = new PromotionRequestDTO();
       dto.setFromClassroomId(oldClassroom.getId());
       dto.setToClassroomId(newClassroom.getId());

       Mockito.when(schoolConfig.requireCurrentSchool()).thenReturn(school);
       Mockito.when(academicYearRepository.findTopByClosedTrueAndSchool_IdOrderByEndDateDesc(school.getId())).thenReturn(Optional.of(oldYear));
       Mockito.when(academicYearRepository.findByIsCurrentAndSchool_Id(true, school.getId())).thenReturn(Optional.of(newYear));
       Mockito.when(classroomRespository.findByIdAndSchool(1L, school)).thenReturn(Optional.of(oldClassroom));
       Mockito.when(classroomRespository.findByIdAndSchool(2L, school)).thenReturn(Optional.of(newClassroom));
       Mockito.when(enrollmentRepository.findByClassroomAndAcademicYearAndStatus(oldClassroom, oldYear, StudentStatus.ACTIVE)).thenReturn(List.of(oldEnrollment));
       Mockito.when(enrollmentRepository.existsByStudentAndAcademicYear(Mockito.any(), Mockito.eq(newYear))).thenReturn(false);
       Mockito.when(adminStudentService.generateNextRollNoForClass(newClassroom, newYear)).thenReturn("R001");

      promotionService.promoteClassroom(dto);







   }

}