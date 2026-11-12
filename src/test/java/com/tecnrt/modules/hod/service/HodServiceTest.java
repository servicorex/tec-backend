package com.tecnrt.modules.hod.service;

import com.tecnrt.exception.UnauthorizedException;
import com.tecnrt.modules.attendance.repository.AttendanceSummaryRepository;
import com.tecnrt.modules.attendance.repository.SubjectRepository;
import com.tecnrt.modules.auth.repository.UserRepository;
import com.tecnrt.modules.hod.dto.CreateSectionRequest;
import com.tecnrt.modules.hod.dto.CreateStudentRequest;
import com.tecnrt.modules.hod.dto.SectionResponse;
import com.tecnrt.modules.hod.entity.Section;
import com.tecnrt.modules.hod.repository.FacultyRepository;
import com.tecnrt.modules.hod.repository.SectionRepository;
import com.tecnrt.modules.student.entity.Student;
import com.tecnrt.modules.student.repository.StudentProfileRepository;
import com.tecnrt.modules.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HodServiceTest {

    @Mock private HodSecurityService hodSecurityService;
    @Mock private StudentRepository studentRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private FacultyRepository facultyRepository;
    @Mock private AttendanceSummaryRepository attendanceSummaryRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private HodService hodService;

    @BeforeEach
    void setUp() {
        hodService = new HodService(
                hodSecurityService,
                studentRepository,
                studentProfileRepository,
                userRepository,
                sectionRepository,
                facultyRepository,
                attendanceSummaryRepository,
                subjectRepository,
                passwordEncoder
        );
    }

    @Test
    void createSection_success() {
        when(hodSecurityService.getAuthenticatedHodDepartment()).thenReturn("CSE");
        when(sectionRepository.existsByDepartmentIdAndSectionName("CSE", "D")).thenReturn(false);

        Section saved = new Section("CSE", "D", "2026-2027");
        when(sectionRepository.save(any(Section.class))).thenReturn(saved);

        SectionResponse res = hodService.createSection(new CreateSectionRequest("D", "2026-2027"));
        assertNotNull(res);
        assertEquals("D", res.sectionName());
        assertEquals("CSE", res.departmentId());
        verify(hodSecurityService).recordAudit(eq("SECTION_CREATE"), any(), eq("Section"), any());
    }

    @Test
    void createSection_duplicateThrows() {
        when(hodSecurityService.getAuthenticatedHodDepartment()).thenReturn("CSE");
        when(sectionRepository.existsByDepartmentIdAndSectionName("CSE", "A")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                hodService.createSection(new CreateSectionRequest("A", "2026-2027")));
    }

    @Test
    void createStudent_success() {
        when(hodSecurityService.getAuthenticatedHodDepartment()).thenReturn("CSE");
        when(studentRepository.existsByRollNumberIgnoreCase("21TML0599")).thenReturn(false);

        Student student = new Student();
        student.setRollNumber("21TML0599");
        student.setFullName("Test Student");
        student.setDepartmentId("CSE");
        student.setYear("4th Year");
        student.setSemester("Sem 4-1");
        student.setSection("A");
        student.setStatus("Active");
        student.setAccountStatus("PENDING");

        when(studentRepository.save(any(Student.class))).thenReturn(student);

        var res = hodService.createStudent(new CreateStudentRequest(
                "21TML0599", "Test Student", "4th Year", "Sem 4-1", "A", "2026", "Convener", false));

        assertNotNull(res);
        assertEquals("21TML0599", res.rollNumber());
        assertEquals("CSE", res.departmentId());
        verify(studentProfileRepository).save(any());
        verify(hodSecurityService).recordAudit(eq("STUDENT_CREATE"), any(), eq("Student"), any());
    }
}

