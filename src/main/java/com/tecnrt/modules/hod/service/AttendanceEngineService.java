package com.tecnrt.modules.hod.service;

import com.tecnrt.exception.ResourceNotFoundException;
import com.tecnrt.exception.UnauthorizedException;
import com.tecnrt.modules.attendance.entity.AttendancePeriod;
import com.tecnrt.modules.attendance.entity.AttendanceRecord;
import com.tecnrt.modules.attendance.entity.AttendanceSummary;
import com.tecnrt.modules.attendance.entity.Subject;
import com.tecnrt.modules.attendance.repository.AttendancePeriodRepository;
import com.tecnrt.modules.attendance.repository.AttendanceRecordRepository;
import com.tecnrt.modules.attendance.repository.AttendanceSummaryRepository;
import com.tecnrt.modules.attendance.repository.SubjectRepository;
import com.tecnrt.modules.hod.dto.AttendancePublishResponse;
import com.tecnrt.modules.hod.dto.AttendanceValidationResponse;
import com.tecnrt.modules.notification.entity.Notification;
import com.tecnrt.modules.notification.repository.NotificationRepository;
import com.tecnrt.modules.student.entity.Student;
import com.tecnrt.modules.student.repository.StudentRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class AttendanceEngineService {

    private final HodSecurityService hodSecurityService;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final AttendancePeriodRepository attendancePeriodRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final NotificationRepository notificationRepository;

    public AttendanceEngineService(
            HodSecurityService hodSecurityService,
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            AttendancePeriodRepository attendancePeriodRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            AttendanceSummaryRepository attendanceSummaryRepository,
            NotificationRepository notificationRepository) {
        this.hodSecurityService = hodSecurityService;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.attendancePeriodRepository = attendancePeriodRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.attendanceSummaryRepository = attendanceSummaryRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Dry-run validation of uploaded attendance CSV without modifying database state.
     */
    @Transactional(readOnly = true)
    public AttendanceValidationResponse validateCsv(
            MultipartFile file, String year, String semester, String section, String periodName) {

        String deptId = hodSecurityService.getAuthenticatedHodDepartment();

        if (file.isEmpty()) {
            return new AttendanceValidationResponse(
                    false,
                    new AttendanceValidationResponse.ValidationSummary(0, 0, 0, 0),
                    List.of(),
                    "Uploaded file is empty"
            );
        }

        List<AttendanceValidationResponse.ValidatedRow> rows = new ArrayList<>();
        Set<String> seenRollNumbers = new HashSet<>();
        int validCount = 0;
        int rejectedCount = 0;
        int warningCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            int rowIdx = 1;
            for (CSVRecord record : parser) {
                rowIdx++;
                String roll = record.isSet(0) ? record.get(0).trim().toUpperCase() : "";
                String name = record.isSet(1) ? record.get(1).trim() : "";
                String subjectCode = record.isSet(6) ? record.get(6).trim().toUpperCase() : "";
                String heldStr = record.isSet(8) ? record.get(8).trim() : "";
                String attendedStr = record.isSet(9) ? record.get(9).trim() : "";
                String absentStr = record.isSet(10) ? record.get(10).trim() : "";

                List<String> errors = new ArrayList<>();
                List<String> warnings = new ArrayList<>();

                // Rule 1: Missing Roll Number
                if (roll.isBlank()) {
                    errors.add("Missing Student Roll Number");
                } else {
                    Optional<Student> studentOpt = studentRepository.findByRollNumberIgnoreCase(roll);
                    if (studentOpt.isEmpty()) {
                        errors.add("Unknown Roll Number [" + roll + "] not registered in college roster");
                    } else {
                        Student s = studentOpt.get();
                        if (!deptId.equalsIgnoreCase(s.getDepartmentId())) {
                            errors.add("Student belongs to department [" + s.getDepartmentId() + "], outside your department [" + deptId + "]");
                        }
                        if (section != null && !section.equalsIgnoreCase(s.getSection())) {
                            errors.add("Student is enrolled in Section [" + s.getSection() + "], not target Section [" + section + "]");
                        }
                    }

                    if (seenRollNumbers.contains(roll + ":" + subjectCode)) {
                        errors.add("Duplicate attendance entry for Roll Number [" + roll + "] and Subject [" + subjectCode + "]");
                    } else {
                        seenRollNumbers.add(roll + ":" + subjectCode);
                    }
                }

                // Rule 2: Numeric checks
                int held = 0;
                int attended = 0;
                int absent = 0;
                BigDecimal pct = BigDecimal.ZERO;

                try {
                    held = Integer.parseInt(heldStr);
                    if (held <= 0) errors.add("Total Classes Held must be greater than zero");
                } catch (NumberFormatException e) {
                    errors.add("Total Classes Held must be a valid integer");
                }

                try {
                    attended = Integer.parseInt(attendedStr);
                    if (attended < 0) errors.add("Classes Attended cannot be negative");
                } catch (NumberFormatException e) {
                    errors.add("Classes Attended must be a valid integer");
                }

                try {
                    absent = Integer.parseInt(absentStr);
                    if (absent < 0) errors.add("Classes Absent cannot be negative");
                } catch (NumberFormatException e) {
                    // Calculate from held - attended if absent was not explicitly provided
                    absent = held - attended;
                }

                // Arithmetic consistency
                if (held > 0 && attended >= 0) {
                    if (attended > held) {
                        errors.add("Classes Attended (" + attended + ") cannot exceed Total Classes Held (" + held + ")");
                    } else if (attended + absent != held) {
                        warnings.add("Classes Attended (" + attended + ") + Absent (" + absent + ") != Total Held (" + held + ")");
                    }

                    pct = BigDecimal.valueOf(attended)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(held), 2, RoundingMode.HALF_UP);
                }

                String status = errors.isEmpty() ? (warnings.isEmpty() ? "VALID" : "WARNING") : "REJECTED";
                if (!errors.isEmpty()) rejectedCount++;
                else if (!warnings.isEmpty()) { warningCount++; validCount++; }
                else validCount++;

                rows.add(new AttendanceValidationResponse.ValidatedRow(
                        rowIdx, roll, name, subjectCode, held, attended, absent, pct, status, errors, warnings
                ));
            }

        } catch (Exception e) {
            return new AttendanceValidationResponse(
                    false,
                    new AttendanceValidationResponse.ValidationSummary(0, 0, 0, 0),
                    List.of(),
                    "Failed to parse CSV: " + e.getMessage()
            );
        }

        boolean isValid = rejectedCount == 0 && !rows.isEmpty();
        return new AttendanceValidationResponse(
                isValid,
                new AttendanceValidationResponse.ValidationSummary(rows.size(), validCount, rejectedCount, warningCount),
                rows,
                isValid ? null : "CSV contains " + rejectedCount + " rejected rows. Please resolve errors before publishing."
        );
    }

    /**
     * Atomically publishes attendance records, updates attendance summary, and notifies students.
     */
    @Transactional
    public AttendancePublishResponse publishAttendance(
            MultipartFile file, String year, String semester, String section, String periodName, String academicYear) {

        String deptId = hodSecurityService.getAuthenticatedHodDepartment();

        // Perform validation first
        AttendanceValidationResponse validation = validateCsv(file, year, semester, section, periodName);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.generalError() != null
                    ? validation.generalError()
                    : "Attendance spreadsheet contains invalid or unverified rows");
        }

        // Create or load AttendancePeriod
        AttendancePeriod period = attendancePeriodRepository.findByDepartmentIdAndYearAndSectionAndPeriodName(
                deptId, year, section, periodName
        ).orElseGet(() -> new AttendancePeriod(
                deptId, year, section, periodName, "monthly",
                academicYear != null ? academicYear : "2026-2027", "PUBLISHED", file.getOriginalFilename()
        ));
        period.setStatus("PUBLISHED");
        period.setFileName(file.getOriginalFilename());
        AttendancePeriod savedPeriod = attendancePeriodRepository.save(period);

        int recordsSaved = 0;
        Map<UUID, StudentAggregate> aggregates = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : parser) {
                String roll = record.isSet(0) ? record.get(0).trim().toUpperCase() : "";
                String subjectCode = record.isSet(6) ? record.get(6).trim().toUpperCase() : "";
                int held = Integer.parseInt(record.get(8).trim());
                int attended = Integer.parseInt(record.get(9).trim());
                int absent = record.isSet(10) && !record.get(10).isBlank()
                        ? Integer.parseInt(record.get(10).trim())
                        : (held - attended);

                BigDecimal pct = BigDecimal.valueOf(attended)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(held), 2, RoundingMode.HALF_UP);

                Student student = studentRepository.findByRollNumberIgnoreCase(roll)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + roll));

                Subject subject = subjectRepository.findBySubjectCodeIgnoreCase(subjectCode)
                        .orElseGet(() -> {
                            Subject s = new Subject(subjectCode, subjectCode + " Course", deptId, year, semester, BigDecimal.valueOf(3.0));
                            return subjectRepository.save(s);
                        });

                // Save or update AttendanceRecord
                Optional<AttendanceRecord> existingRec = attendanceRecordRepository.findByPeriodIdAndStudentIdAndSubjectId(
                        savedPeriod.getId(), student.getId(), subject.getId()
                );

                AttendanceRecord rec;
                if (existingRec.isPresent()) {
                    rec = existingRec.get();
                    rec.setClassesHeld(held);
                    rec.setClassesAttended(attended);
                    rec.setClassesAbsent(absent);
                    rec.setAttendancePercentage(pct);
                } else {
                    rec = new AttendanceRecord(savedPeriod, student, subject, held, attended, absent, pct);
                }
                attendanceRecordRepository.save(rec);
                recordsSaved++;

                // Track aggregates per student
                aggregates.computeIfAbsent(student.getId(), k -> new StudentAggregate(student))
                        .add(held, attended, absent);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error committing attendance records: " + e.getMessage(), e);
        }

        // Compute and save AttendanceSummary per student
        int summariesCalculated = 0;
        for (StudentAggregate agg : aggregates.values()) {
            BigDecimal overallPct = agg.totalHeld > 0
                    ? BigDecimal.valueOf(agg.totalAttended)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(agg.totalHeld), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            String status = overallPct.compareTo(BigDecimal.valueOf(75.0)) >= 0
                    ? "ELIGIBLE"
                    : (overallPct.compareTo(BigDecimal.valueOf(65.0)) >= 0 ? "CONDONATION" : "DETAINED");

            Optional<AttendanceSummary> existingSum = attendanceSummaryRepository.findByPeriodIdAndStudentId(
                    savedPeriod.getId(), agg.student.getId()
            );

            AttendanceSummary summary;
            if (existingSum.isPresent()) {
                summary = existingSum.get();
                summary.setTotalClassesHeld(agg.totalHeld);
                summary.setTotalClassesAttended(agg.totalAttended);
                summary.setTotalClassesAbsent(agg.totalAbsent);
                summary.setOverallPercentage(overallPct);
                if (!summary.isOverridden()) {
                    summary.setStatus(status);
                }
            } else {
                summary = new AttendanceSummary(
                        savedPeriod, agg.student, agg.totalHeld, agg.totalAttended, agg.totalAbsent, overallPct, status
                );
            }
            attendanceSummaryRepository.save(summary);
            summariesCalculated++;
        }

        // Post official Notification for students
        Notification notif = new Notification(
                deptId,
                year,
                section,
                "Official Attendance Published: " + periodName,
                "Attendance register for " + year + " Section " + section + " (" + periodName + ") has been officially validated and published by HOD."
        );
        notificationRepository.save(notif);

        // Record Audit log
        hodSecurityService.recordAudit(
                "ATTENDANCE_PUBLISH",
                "Published attendance for " + deptId + " " + year + " Sec " + section + " (" + periodName + ") - " + recordsSaved + " records",
                "AttendancePeriod",
                savedPeriod.getId().toString()
        );

        return new AttendancePublishResponse(
                savedPeriod.getId(),
                deptId,
                year,
                section,
                periodName,
                recordsSaved,
                summariesCalculated,
                "PUBLISHED",
                OffsetDateTime.now()
        );
    }

    private static class StudentAggregate {
        final Student student;
        int totalHeld = 0;
        int totalAttended = 0;
        int totalAbsent = 0;

        StudentAggregate(Student student) {
            this.student = student;
        }

        void add(int held, int attended, int absent) {
            this.totalHeld += held;
            this.totalAttended += attended;
            this.totalAbsent += absent;
        }
    }
}

