package com.tecnrt.modules.student.service;

import com.tecnrt.modules.attendance.repository.AttendanceRecordRepository;
import com.tecnrt.modules.attendance.repository.AttendanceSummaryRepository;
import com.tecnrt.modules.attendance.repository.SubjectRepository;
import com.tecnrt.modules.auth.entity.User;
import com.tecnrt.modules.notification.entity.Notification;
import com.tecnrt.modules.notification.repository.NotificationRepository;
import com.tecnrt.modules.student.dto.StudentAttendanceResponse;
import com.tecnrt.modules.student.dto.StudentDashboardResponse;
import com.tecnrt.modules.student.dto.StudentProfileResponse;
import com.tecnrt.modules.student.dto.StudentProfileUpdateRequest;
import com.tecnrt.modules.student.entity.Student;
import com.tecnrt.modules.student.entity.StudentProfile;
import com.tecnrt.modules.student.repository.StudentProfileRepository;
import com.tecnrt.modules.student.repository.StudentRepository;
import com.tecnrt.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private AttendanceSummaryRepository attendanceSummaryRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private NotificationRepository notificationRepository;

    private StudentService studentService;

    private UUID userId;
    private Student testStudent;
    private StudentProfile testProfile;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(
                studentRepository,
                studentProfileRepository,
                attendanceSummaryRepository,
                attendanceRecordRepository,
                subjectRepository,
                notificationRepository
        );

        userId = UUID.randomUUID();
        User user = new User("21TML0501", "hash", "STUDENT");
        user.setId(userId);

        testStudent = new Student();
        testStudent.setId(UUID.randomUUID());
        testStudent.setRollNumber("21TML0501");
        testStudent.setFullName("M. Sai Vardhan");
        testStudent.setUser(user);
        testStudent.setDepartmentId("CSE");
        testStudent.setYear("IV Year");
        testStudent.setSemester("Sem 4-1");
        testStudent.setSection("A");
        testStudent.setCgpa(BigDecimal.valueOf(8.64));
        testStudent.setAdmissionYear("2021");
        testStudent.setAdmissionType("Convener Quota (EAPCET)");

        testProfile = new StudentProfile();
        testProfile.setId(UUID.randomUUID());
        testProfile.setStudent(testStudent);
        testProfile.setDob(LocalDate.of(2003, 8, 14));
        testProfile.setGender("Male");
        testProfile.setBloodGroup("O+");
        testProfile.setStudentMobile("+91 98480 12345");
        testProfile.setFatherName("M. Venkata Subbaiah");
        testProfile.setFatherPhone("+91 94401 23456");
        testProfile.setMotherName("M. Lakshmi Devi");
        testProfile.setCurrentAddress("Plot 42, Arundelpet, Guntur, AP - 522002");
        testProfile.setPermanentAddress("Plot 42, Arundelpet, Guntur, AP - 522002");

        testStudent.setProfile(testProfile);

        // Mock Security Context for authenticated student
        UserPrincipal principal = new UserPrincipal(userId, "21TML0501", "pass", "STUDENT", 1, true, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        when(studentRepository.findByUserIdWithProfile(userId)).thenReturn(Optional.of(testStudent));
    }

    @Test
    void testGetDashboardReturnsAuthenticatedStudentData() {
        when(attendanceSummaryRepository.findLatestPublishedByStudentId(testStudent.getId())).thenReturn(Optional.empty());
        when(subjectRepository.findByDepartmentIdAndSemesterAndActiveTrue("CSE", "Sem 4-1")).thenReturn(List.of());
        when(attendanceRecordRepository.findByStudentIdWithDetails(testStudent.getId())).thenReturn(List.of());

        StudentDashboardResponse dashboard = studentService.getDashboard();

        assertNotNull(dashboard);
        assertEquals("21TML0501", dashboard.rollNumber());
        assertEquals("M. Sai Vardhan", dashboard.name());
        assertEquals("CSE", dashboard.department());
        assertEquals(BigDecimal.valueOf(8.64), dashboard.cgpa());
        assertEquals("FIRST CLASS WITH DIST.", dashboard.academicStanding());
    }

    @Test
    void testProfileUpdateAndCompletionPercentage() {
        StudentProfileUpdateRequest updateReq = new StudentProfileUpdateRequest(
                "Sai",
                LocalDate.of(2003, 8, 14),
                "Male",
                "O+",
                "+91 98480 12345",
                "+91 94401 23456",
                "father@gmail.com",
                "Plot 42, Arundelpet",
                "Plot 42, Arundelpet",
                "M. Venkata Subbaiah",
                "Business",
                "M. Lakshmi Devi",
                "Homemaker",
                null,
                null
        );

        when(studentProfileRepository.save(any())).thenReturn(testProfile);
        when(studentRepository.save(any())).thenReturn(testStudent);

        StudentProfileResponse updated = studentService.updateProfile(updateReq);

        assertNotNull(updated);
        assertEquals(100, updated.profileCompletion());
        assertEquals("Complete", updated.profileStatus());
        assertEquals("21TML0501", updated.rollNumber()); // Immutable field preserved
    }

    @Test
    void testLateralEntryAttendanceReturnsNotApplicableForFirstYear() {
        testStudent.setLateralEntry(true); // Lateral Entry student

        when(attendanceSummaryRepository.findByStudentIdWithPeriod(testStudent.getId())).thenReturn(List.of());
        when(attendanceSummaryRepository.findLatestPublishedByStudentId(testStudent.getId())).thenReturn(Optional.empty());

        StudentAttendanceResponse attendance = studentService.getAttendance();

        assertNotNull(attendance);
        assertTrue(attendance.isLateralEntry());

        // Verify Sem 1-1 and Sem 1-2 return NOT_APPLICABLE
        StudentAttendanceResponse.SemesterAttendanceTabDto sem11 = attendance.allSemesters().get(0);
        StudentAttendanceResponse.SemesterAttendanceTabDto sem12 = attendance.allSemesters().get(1);
        StudentAttendanceResponse.SemesterAttendanceTabDto sem21 = attendance.allSemesters().get(2);

        assertEquals("1-1", sem11.code());
        assertEquals("NOT_APPLICABLE", sem11.status());
        assertFalse(sem11.isApplicable());

        assertEquals("1-2", sem12.code());
        assertEquals("NOT_APPLICABLE", sem12.status());
        assertFalse(sem12.isApplicable());

        assertEquals("2-1", sem21.code());
        assertTrue(sem21.isApplicable()); // Applicable from 2nd year onwards
    }

    @Test
    void testNotificationsTargetedToDepartmentAndSection() {
        Notification n = new Notification("CSE", "IV Year", "A", "Class Notice", "Campus interview tomorrow");
        when(notificationRepository.findForStudent("CSE", "IV Year", "A")).thenReturn(List.of(n));

        List<Notification> notifs = studentService.getNotifications();

        assertNotNull(notifs);
        assertEquals(1, notifs.size());
        assertEquals("Class Notice", notifs.get(0).getTitle());
    }
}

