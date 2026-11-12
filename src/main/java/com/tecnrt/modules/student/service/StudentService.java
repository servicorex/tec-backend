package com.tecnrt.modules.student.service;

import com.tecnrt.exception.ResourceNotFoundException;
import com.tecnrt.exception.UnauthorizedException;
import com.tecnrt.modules.attendance.entity.AttendancePeriod;
import com.tecnrt.modules.attendance.entity.AttendanceRecord;
import com.tecnrt.modules.attendance.entity.AttendanceSummary;
import com.tecnrt.modules.attendance.entity.Subject;
import com.tecnrt.modules.attendance.repository.AttendanceRecordRepository;
import com.tecnrt.modules.attendance.repository.AttendanceSummaryRepository;
import com.tecnrt.modules.attendance.repository.SubjectRepository;
import com.tecnrt.modules.notification.entity.Notification;
import com.tecnrt.modules.notification.repository.NotificationRepository;
import com.tecnrt.modules.student.dto.*;
import com.tecnrt.modules.student.entity.Student;
import com.tecnrt.modules.student.entity.StudentProfile;
import com.tecnrt.modules.student.repository.StudentProfileRepository;
import com.tecnrt.modules.student.repository.StudentRepository;
import com.tecnrt.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final SubjectRepository subjectRepository;
    private final NotificationRepository notificationRepository;

    public StudentService(
            StudentRepository studentRepository,
            StudentProfileRepository studentProfileRepository,
            AttendanceSummaryRepository attendanceSummaryRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            SubjectRepository subjectRepository,
            NotificationRepository notificationRepository) {
        this.studentRepository = studentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.attendanceSummaryRepository = attendanceSummaryRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.subjectRepository = subjectRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Resolves the authenticated student entity strictly through the server-side JWT principal.
     */
    public Student getAuthenticatedStudent() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }
        return studentRepository.findByUserIdWithProfile(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student record not found for the authenticated user"));
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getDashboard() {
        Student student = getAuthenticatedStudent();

        // Attendance stats from latest published summary
        Optional<AttendanceSummary> summaryOpt = attendanceSummaryRepository.findLatestPublishedByStudentId(student.getId());

        BigDecimal overallAttendance = summaryOpt.map(AttendanceSummary::getOverallPercentage).orElse(BigDecimal.ZERO);
        int held = summaryOpt.map(AttendanceSummary::getTotalClassesHeld).orElse(0);
        int attended = summaryOpt.map(AttendanceSummary::getTotalClassesAttended).orElse(0);
        int absent = summaryOpt.map(AttendanceSummary::getTotalClassesAbsent).orElse(0);
        String status = summaryOpt.map(AttendanceSummary::getStatus).orElse("NO_DATA");

        // Enrolled subjects for student's active semester
        List<Subject> subjects = subjectRepository.findByDepartmentIdAndSemesterAndActiveTrue(
                student.getDepartmentId(), student.getSemester());

        // Map subject attendance if recorded
        List<AttendanceRecord> records = attendanceRecordRepository.findByStudentIdWithDetails(student.getId());
        Map<String, AttendanceRecord> recordBySubjectCode = new HashMap<>();
        for (AttendanceRecord r : records) {
            recordBySubjectCode.put(r.getSubject().getSubjectCode(), r);
        }

        BigDecimal totalCredits = BigDecimal.ZERO;
        List<StudentDashboardResponse.EnrolledSubjectDto> subjectDtos = new ArrayList<>();

        for (Subject sub : subjects) {
            totalCredits = totalCredits.add(sub.getCredits());
            AttendanceRecord rec = recordBySubjectCode.get(sub.getSubjectCode());

            int subHeld = rec != null ? rec.getClassesHeld() : 0;
            int subAttended = rec != null ? rec.getClassesAttended() : 0;
            BigDecimal subPct = rec != null ? rec.getAttendancePercentage() : BigDecimal.ZERO;

            subjectDtos.add(new StudentDashboardResponse.EnrolledSubjectDto(
                    sub.getSubjectCode(),
                    sub.getSubjectName(),
                    "Faculty In-Charge",
                    sub.getCredits(),
                    subHeld,
                    subAttended,
                    subPct,
                    "85%"
            ));
        }

        // Academic standing string
        String standing = (student.getCgpa() != null && student.getCgpa().compareTo(BigDecimal.valueOf(8.0)) >= 0)
                ? "FIRST CLASS WITH DIST." : "FIRST CLASS";

        // Hall ticket notice
        StudentDashboardResponse.ExamNoticeDto examNotice = new StudentDashboardResponse.ExamNoticeDto(
                student.getSemester() + " Regular Examinations",
                "Hall Ticket Released",
                student.getRollNumber(),
                "Tirumala Engineering College, Narasaraopet (Center Code: TMLN)",
                true
        );

        return new StudentDashboardResponse(
                student.getFullName(),
                student.getRollNumber(),
                student.getDepartmentId(),
                student.getYear(),
                student.getSemester(),
                student.getSection(),
                overallAttendance,
                held,
                attended,
                absent,
                status,
                student.getCgpa() != null ? student.getCgpa() : BigDecimal.ZERO,
                BigDecimal.valueOf(8.5), // SGPA
                standing,
                subjects.size(),
                totalCredits,
                subjectDtos,
                examNotice
        );
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile() {
        Student student = getAuthenticatedStudent();
        StudentProfile p = student.getProfile();

        return new StudentProfileResponse(
                student.getRollNumber(),
                student.getAdmissionNumber(),
                student.getDepartmentId(),
                student.getYear(),
                student.getSemester(),
                student.getSection(),
                student.getCourse(),
                student.getAdmissionYear(),
                student.getAdmissionDate(),
                student.getAdmissionType(),
                student.getAdmissionQuota(),
                student.getCasteCategory(),
                student.isLateralEntry(),
                student.getStatus(),
                student.getAccountStatus(),
                student.getProfileStatus(),
                student.getProfileCompletion(),
                student.getFullName(),
                student.getShortName(),
                p != null ? p.getDob() : null,
                p != null ? p.getGender() : null,
                p != null ? p.getBloodGroup() : null,
                p != null ? p.getStudentMobile() : null,
                student.getRollNumber().toLowerCase() + "@tecnrt.org",
                p != null ? p.getFatherPhone() : null,
                p != null ? p.getFatherEmail() : null,
                p != null ? p.getCurrentAddress() : null,
                p != null ? p.getPermanentAddress() : null,
                p != null ? p.getFatherName() : null,
                p != null ? p.getFatherOccupation() : null,
                p != null ? p.getMotherName() : null,
                p != null ? p.getMotherOccupation() : null,
                p != null ? p.getGuardianName() : null,
                p != null ? p.getGuardianContact() : null,
                p != null ? p.getScholarshipStatus() : "ELIGIBLE",
                p != null ? p.getFeeReimbursementStatus() : "FULL (JVD)"
        );
    }

    @Transactional
    public StudentProfileResponse updateProfile(StudentProfileUpdateRequest req) {
        Student student = getAuthenticatedStudent();
        StudentProfile p = student.getProfile();
        if (p == null) {
            p = new StudentProfile();
            p.setStudent(student);
            student.setProfile(p);
        }

        // Whitelisted editable fields only
        if (req.shortName() != null) student.setShortName(req.shortName().trim());
        if (req.dob() != null) p.setDob(req.dob());
        if (req.gender() != null) p.setGender(req.gender().trim());
        if (req.bloodGroup() != null) p.setBloodGroup(req.bloodGroup().trim());
        if (req.studentMobile() != null) p.setStudentMobile(req.studentMobile().trim());
        if (req.fatherPhone() != null) p.setFatherPhone(req.fatherPhone().trim());
        if (req.fatherEmail() != null) p.setFatherEmail(req.fatherEmail().trim());
        if (req.currentAddress() != null) p.setCurrentAddress(req.currentAddress().trim());
        if (req.permanentAddress() != null) p.setPermanentAddress(req.permanentAddress().trim());
        if (req.fatherName() != null) p.setFatherName(req.fatherName().trim());
        if (req.fatherOccupation() != null) p.setFatherOccupation(req.fatherOccupation().trim());
        if (req.motherName() != null) p.setMotherName(req.motherName().trim());
        if (req.motherOccupation() != null) p.setMotherOccupation(req.motherOccupation().trim());
        if (req.guardianName() != null) p.setGuardianName(req.guardianName().trim());
        if (req.guardianContact() != null) p.setGuardianContact(req.guardianContact().trim());

        // Calculate profile completion percentage based on the 13 required frontend fields
        int completionPct = calculateProfileCompletion(student, p);
        student.setProfileCompletion(completionPct);
        student.setProfileStatus(completionPct == 100 ? "Complete" : "Incomplete");

        studentProfileRepository.save(p);
        studentRepository.save(student);

        return getProfile();
    }

    @Transactional(readOnly = true)
    public StudentAttendanceResponse getAttendance() {
        Student student = getAuthenticatedStudent();

        // Define 8 canonical semesters
        String[] semCodes = {"1-1", "1-2", "2-1", "2-2", "3-1", "3-2", "4-1", "4-2"};
        String[] semYears = {"1st Year", "1st Year", "2nd Year", "2nd Year", "3rd Year", "3rd Year", "4th Year", "4th Year"};
        String[] semNames = {
                "1st Year I Semester (1-1)", "1st Year II Semester (1-2)",
                "2nd Year I Semester (2-1)", "2nd Year II Semester (2-2)",
                "3rd Year I Semester (3-1)", "3rd Year II Semester (3-2)",
                "4th Year I Semester (4-1)", "4th Year II Semester (4-2)"
        };

        List<AttendanceSummary> allSummaries = attendanceSummaryRepository.findByStudentIdWithPeriod(student.getId());
        Map<String, AttendanceSummary> summaryByPeriodName = new HashMap<>();
        for (AttendanceSummary s : allSummaries) {
            summaryByPeriodName.put(s.getPeriod().getPeriodName(), s);
        }

        List<StudentAttendanceResponse.SemesterAttendanceTabDto> tabs = new ArrayList<>();

        for (int i = 0; i < semCodes.length; i++) {
            String code = semCodes[i];
            boolean isApplicable = !(student.isLateralEntry() && (code.equals("1-1") || code.equals("1-2")));

            if (!isApplicable) {
                tabs.add(new StudentAttendanceResponse.SemesterAttendanceTabDto(
                        code, code, semNames[i], semYears[i], false, false, BigDecimal.ZERO, "NOT_APPLICABLE"
                ));
            } else {
                AttendanceSummary semSummary = summaryByPeriodName.get("Semester " + code);
                boolean hasData = semSummary != null;
                BigDecimal pct = hasData ? semSummary.getOverallPercentage() : BigDecimal.ZERO;
                String tabStatus = hasData ? semSummary.getStatus() : "NO_DATA";

                tabs.add(new StudentAttendanceResponse.SemesterAttendanceTabDto(
                        code, code, semNames[i], semYears[i], hasData, true, pct, tabStatus
                ));
            }
        }

        // Active semester metrics
        Optional<AttendanceSummary> latestSummaryOpt = attendanceSummaryRepository.findLatestPublishedByStudentId(student.getId());

        int totalConducted = latestSummaryOpt.map(AttendanceSummary::getTotalClassesHeld).orElse(0);
        int totalAttended = latestSummaryOpt.map(AttendanceSummary::getTotalClassesAttended).orElse(0);
        int totalAbsent = latestSummaryOpt.map(AttendanceSummary::getTotalClassesAbsent).orElse(0);
        BigDecimal activePct = latestSummaryOpt.map(AttendanceSummary::getOverallPercentage).orElse(BigDecimal.ZERO);
        String activeStatus = latestSummaryOpt.map(AttendanceSummary::getStatus).orElse("NO_DATA");

        // Monthly breakdown
        List<StudentAttendanceResponse.MonthlyAttendanceRecordDto> monthlyList = new ArrayList<>();
        for (AttendanceSummary s : allSummaries) {
            if ("monthly".equalsIgnoreCase(s.getPeriod().getPeriodType())) {
                monthlyList.add(new StudentAttendanceResponse.MonthlyAttendanceRecordDto(
                        s.getPeriod().getPeriodName(),
                        s.getTotalClassesHeld(),
                        s.getTotalClassesAttended(),
                        s.getTotalClassesAbsent(),
                        s.getOverallPercentage()
                ));
            }
        }

        StudentAttendanceResponse.ActiveSemesterAttendanceDto activeDto =
                new StudentAttendanceResponse.ActiveSemesterAttendanceDto(
                        totalConducted, totalAttended, totalAbsent, activePct, activeStatus, monthlyList
                );

        return new StudentAttendanceResponse(
                student.getSemester(),
                student.getYear() + " - " + student.getSemester(),
                student.isLateralEntry(),
                tabs,
                activeDto
        );
    }

    @Transactional(readOnly = true)
    public StudentExamsResponse getExams() {
        Student student = getAuthenticatedStudent();

        // Matches ExaminationsView.jsx structure
        return new StudentExamsResponse(
                student.getCourse() + " " + student.getYear() + " Regular (JNTUK R20)",
                student.getRollNumber(),
                "Tirumala Engineering College, Narasaraopet (Center Code: TMLN)",
                true,
                List.of(), // Regular schedules released during active examination windows
                List.of()  // Clean supplementary history (no active backlogs)
        );
    }

    @Transactional(readOnly = true)
    public StudentFeesResponse getFees() {
        Student student = getAuthenticatedStudent();

        // Matches FeesPaymentView.jsx advisory model
        return new StudentFeesResponse(
                "2026-2027",
                student.getFullName(),
                student.getRollNumber(),
                student.getDepartmentId() + " - " + student.getYear(),
                BigDecimal.valueOf(65000),
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(15000),
                "PRD Section 10 Payment Compliance: The portal preserves official college bank counters & SBI Collect. Receipts are cryptographically verified upon server-side confirmation.",
                List.of(
                        new StudentFeesResponse.FeeItemDto("fee-1", "Tuition Fee (Annual)", BigDecimal.valueOf(50000), BigDecimal.valueOf(50000), BigDecimal.ZERO, "2026-08-31", "Paid"),
                        new StudentFeesResponse.FeeItemDto("fee-2", "University & Exam Registration Fee", BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.valueOf(5000), "2026-10-30", "Pending"),
                        new StudentFeesResponse.FeeItemDto("fee-3", "College Transport (Bus Route 4 - Vinukonda)", BigDecimal.valueOf(7000), BigDecimal.ZERO, BigDecimal.valueOf(7000), "2026-11-15", "Pending"),
                        new StudentFeesResponse.FeeItemDto("fee-4", "Library & Digital Learning Access", BigDecimal.valueOf(3000), BigDecimal.ZERO, BigDecimal.valueOf(3000), "2026-11-15", "Pending")
                ),
                List.of(
                        new StudentFeesResponse.FeeTransactionDto("TXN-TMLN-2026-88421", "2026-08-14 11:24 AM", "Tuition Fee - Term I", BigDecimal.valueOf(30000), "UPI (Google Pay)", "REC-2026-0941", "Success"),
                        new StudentFeesResponse.FeeTransactionDto("TXN-TMLN-2026-92144", "2026-09-02 03:15 PM", "Tuition Fee - Term II", BigDecimal.valueOf(20000), "Net Banking (HDFC)", "REC-2026-1218", "Success")
                )
        );
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotifications() {
        Student student = getAuthenticatedStudent();
        return notificationRepository.findForStudent(student.getDepartmentId(), student.getYear(), student.getSection());
    }

    /**
     * Exact 13-field completion calculation matching frontend mockData.js line 196
     */
    private int calculateProfileCompletion(Student s, StudentProfile p) {
        String[] fields = {
                s.getFullName(),
                p != null && p.getDob() != null ? p.getDob().toString() : null,
                p != null ? p.getGender() : null,
                p != null ? p.getBloodGroup() : null,
                p != null ? p.getStudentMobile() : null,
                s.getRollNumber() != null ? s.getRollNumber().toLowerCase() + "@tecnrt.org" : null,
                p != null ? p.getFatherName() : null,
                p != null ? p.getFatherPhone() : null,
                p != null ? p.getMotherName() : null,
                p != null ? p.getCurrentAddress() : null,
                p != null ? p.getPermanentAddress() : null,
                s.getAdmissionYear(),
                s.getAdmissionType()
        };

        long filled = Arrays.stream(fields)
                .filter(f -> f != null && !f.trim().isEmpty() && !f.equalsIgnoreCase("Not Specified"))
                .count();

        return (int) Math.round(((double) filled / fields.length) * 100);
    }
}

