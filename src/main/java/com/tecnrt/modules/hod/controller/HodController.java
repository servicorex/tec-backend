package com.tecnrt.modules.hod.controller;

import com.tecnrt.modules.hod.dto.*;
import com.tecnrt.modules.hod.service.AttendanceEngineService;
import com.tecnrt.modules.hod.service.HodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hod")
@PreAuthorize("hasRole('HOD')")
@Tag(name = "HOD Administration", description = "Head of Department administrative endpoints for attendance, student provisioning, and statutory compliance")
public class HodController {

    private final HodService hodService;
    private final AttendanceEngineService attendanceEngineService;

    public HodController(HodService hodService, AttendanceEngineService attendanceEngineService) {
        this.hodService = hodService;
        this.attendanceEngineService = attendanceEngineService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get Department Overview", description = "Returns student counts, average attendance %, and JNTU statutory distributions")
    public ResponseEntity<HodOverviewResponse> getOverview() {
        return ResponseEntity.ok(hodService.getOverview());
    }

    @GetMapping("/sections")
    @Operation(summary = "Get Department Sections", description = "Returns active sections (A, B, C...) for the authenticated HOD's department")
    public ResponseEntity<List<SectionResponse>> getSections() {
        return ResponseEntity.ok(hodService.getSections());
    }

    @PostMapping("/sections")
    @Operation(summary = "Create New Section (+ Add Sec)", description = "Adds a new section to the department (e.g. 'D')")
    public ResponseEntity<SectionResponse> createSection(@Valid @RequestBody CreateSectionRequest request) {
        return ResponseEntity.ok(hodService.createSection(request));
    }

    @GetMapping("/students")
    @Operation(summary = "Get Department Students", description = "Lists students scoped to the HOD's department, optionally filtered by year and section")
    public ResponseEntity<List<HodStudentResponse>> getStudents(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String section) {
        return ResponseEntity.ok(hodService.getStudents(year, section));
    }

    @PostMapping("/students")
    @Operation(summary = "Create Student Record", description = "Manually registers a student into the HOD's department")
    public ResponseEntity<HodStudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.ok(hodService.createStudent(request));
    }

    @PostMapping("/students/provision/batch")
    @Operation(summary = "Batch Provision Student Accounts", description = "Provisions login accounts for selected roll numbers using default credentials (ID=Password) with mandatory first-login password change")
    public ResponseEntity<ProvisionResultResponse> batchProvision(@Valid @RequestBody BatchProvisionRequest request) {
        return ResponseEntity.ok(hodService.batchProvision(request));
    }

    @PostMapping(value = "/students/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk Import Students (2-Column CSV)", description = "Imports students from standard 2-column CSV (Roll Number, Full Name) into a target section")
    public ResponseEntity<BulkImportStudentResult> importStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") String year,
            @RequestParam("semester") String semester,
            @RequestParam("section") String section) {
        return ResponseEntity.ok(hodService.importStudentsCsv(file, year, semester, section));
    }

    @PostMapping(value = "/attendance/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Validate Attendance CSV (Dry Run)", description = "Validates uploaded attendance spreadsheet row-by-row without database persistence")
    public ResponseEntity<AttendanceValidationResponse> validateAttendance(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") String year,
            @RequestParam("semester") String semester,
            @RequestParam("section") String section,
            @RequestParam("period") String period) {
        return ResponseEntity.ok(attendanceEngineService.validateCsv(file, year, semester, section, period));
    }

    @PostMapping(value = "/attendance/publish", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Publish Attendance Records", description = "Transactionally saves verified attendance records, calculates student summaries, and issues notification alerts")
    public ResponseEntity<AttendancePublishResponse> publishAttendance(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") String year,
            @RequestParam("semester") String semester,
            @RequestParam("section") String section,
            @RequestParam("period") String period,
            @RequestParam(value = "academicYear", required = false) String academicYear) {
        return ResponseEntity.ok(attendanceEngineService.publishAttendance(file, year, semester, section, period, academicYear));
    }

    @GetMapping("/faculty")
    @Operation(summary = "Get Department Faculty", description = "Lists active faculty members in the department")
    public ResponseEntity<List<FacultyResponse>> getFaculty() {
        return ResponseEntity.ok(hodService.getFaculty());
    }

    @GetMapping("/reports/statutory")
    @Operation(summary = "Get Statutory Attendance Report", description = "Returns JNTU compliance report: eligible, condonation, and detained students with override capabilities")
    public ResponseEntity<StatutoryReportResponse> getStatutoryReport(
            @RequestParam(required = false) String semester) {
        return ResponseEntity.ok(hodService.getStatutoryReport(semester));
    }

    @PostMapping("/reports/override")
    @Operation(summary = "Apply Statutory Detention Override", description = "HOD override for detention status with mandatory justification and audit logging")
    public ResponseEntity<StatutoryReportResponse.StatutoryStudentRow> applyDetentionOverride(
            @Valid @RequestBody DetentionOverrideRequest request) {
        return ResponseEntity.ok(hodService.applyDetentionOverride(request));
    }

    @GetMapping("/subjects")
    @Operation(summary = "Get Department Subjects Curriculum", description = "Lists active subjects for department, optionally filtered by semester and regulation (e.g. R20, R23)")
    public ResponseEntity<List<SubjectResponse>> getSubjects(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String regulation) {
        return ResponseEntity.ok(hodService.getSubjects(semester, regulation));
    }

    @PostMapping("/subjects")
    @Operation(summary = "Create or Update Subject / Regulation", description = "Allows HOD to dynamically configure or update course curriculum per semester and regulation")
    public ResponseEntity<SubjectResponse> saveSubject(@Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(hodService.saveSubject(request));
    }
}

