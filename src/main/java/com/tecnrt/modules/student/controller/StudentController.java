package com.tecnrt.modules.student.controller;

import com.tecnrt.modules.notification.entity.Notification;
import com.tecnrt.modules.student.dto.*;
import com.tecnrt.modules.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student Portal", description = "Student-facing endpoints for dashboard, profile, attendance, exams, fees, and alerts")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get Student Dashboard", description = "Returns student overview KPI metrics, overall attendance %, CGPA, enrolled subjects, and exam notices")
    public ResponseEntity<StudentDashboardResponse> getDashboard() {
        return ResponseEntity.ok(studentService.getDashboard());
    }

    @GetMapping("/profile")
    @Operation(summary = "Get Student Profile", description = "Returns full personal, parent, contact, and official academic identifiers")
    public ResponseEntity<StudentProfileResponse> getProfile() {
        return ResponseEntity.ok(studentService.getProfile());
    }

    @PutMapping("/profile")
    @Operation(summary = "Update Student Profile", description = "Updates editable personal contact details and recalculates profile completion % (0-100). Academic fields are protected.")
    public ResponseEntity<StudentProfileResponse> updateProfile(@Valid @RequestBody StudentProfileUpdateRequest request) {
        return ResponseEntity.ok(studentService.updateProfile(request));
    }

    @GetMapping("/attendance")
    @Operation(summary = "Get 8-Semester Attendance", description = "Returns 8-semester historical matrix, active semester metrics, and monthly registers with Lateral Entry support")
    public ResponseEntity<StudentAttendanceResponse> getAttendance() {
        return ResponseEntity.ok(studentService.getAttendance());
    }

    @GetMapping("/exams")
    @Operation(summary = "Get Examinations Information", description = "Returns regular schedules, hall ticket eligibility, and supplementary records")
    public ResponseEntity<StudentExamsResponse> getExams() {
        return ResponseEntity.ok(studentService.getExams());
    }

    @GetMapping("/fees")
    @Operation(summary = "Get Fee Obligations (Read-Only)", description = "Returns advisory fee breakdown, dues, and payment transaction receipts")
    public ResponseEntity<StudentFeesResponse> getFees() {
        return ResponseEntity.ok(studentService.getFees());
    }

    @GetMapping("/notifications")
    @Operation(summary = "Get Student Notifications", description = "Returns header bell notifications targeted to student's department, year, and section")
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(studentService.getNotifications());
    }
}

