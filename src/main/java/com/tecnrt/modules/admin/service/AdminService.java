package com.tecnrt.modules.admin.service;

import com.tecnrt.exception.ResourceNotFoundException;
import com.tecnrt.modules.admin.dto.AuditLogResponse;
import com.tecnrt.modules.admin.dto.CollegeOverviewResponse;
import com.tecnrt.modules.admin.dto.CreateHodRequest;
import com.tecnrt.modules.attendance.entity.AttendanceSummary;
import com.tecnrt.modules.attendance.repository.AttendanceSummaryRepository;
import com.tecnrt.modules.auth.entity.User;
import com.tecnrt.modules.auth.repository.UserRepository;
import com.tecnrt.modules.hod.entity.AuditLog;
import com.tecnrt.modules.hod.entity.Faculty;
import com.tecnrt.modules.hod.entity.HodAssignment;
import com.tecnrt.modules.hod.repository.AuditLogRepository;
import com.tecnrt.modules.hod.repository.FacultyRepository;
import com.tecnrt.modules.hod.repository.HodAssignmentRepository;
import com.tecnrt.modules.student.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final HodAssignmentRepository hodAssignmentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            StudentRepository studentRepository,
            FacultyRepository facultyRepository,
            AttendanceSummaryRepository attendanceSummaryRepository,
            HodAssignmentRepository hodAssignmentRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.attendanceSummaryRepository = attendanceSummaryRepository;
        this.hodAssignmentRepository = hodAssignmentRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public CollegeOverviewResponse getCollegeOverview() {
        long totalStudents = studentRepository.count();
        long totalFaculty = facultyRepository.count();

        List<AttendanceSummary> allSummaries = attendanceSummaryRepository.findAll();

        long eligible = 0;
        long condonation = 0;
        long detained = 0;
        BigDecimal sumPct = BigDecimal.ZERO;

        for (AttendanceSummary s : allSummaries) {
            sumPct = sumPct.add(s.getOverallPercentage());
            String st = s.getStatus();
            if ("ELIGIBLE".equalsIgnoreCase(st) || "ELIGIBLE_BY_OVERRIDE".equalsIgnoreCase(st)) eligible++;
            else if ("CONDONATION".equalsIgnoreCase(st)) condonation++;
            else if ("DETAINED".equalsIgnoreCase(st)) detained++;
        }

        BigDecimal collegeAvg = allSummaries.isEmpty()
                ? BigDecimal.ZERO
                : sumPct.divide(BigDecimal.valueOf(allSummaries.size()), 2, RoundingMode.HALF_UP);

        // Predefined active academic departments (Excluded: ASH, CIVIL, MECH)
        String[] depts = {"CSE", "ECE", "EEE", "IT"};
        List<CollegeOverviewResponse.DepartmentMetricDto> deptMetrics = new ArrayList<>();

        for (String d : depts) {
            long sCount = studentRepository.countByDepartmentId(d);
            long fCount = facultyRepository.findByDepartmentIdAndActiveTrueOrderByFullNameAsc(d).size();
            List<AttendanceSummary> dSummaries = attendanceSummaryRepository.findByDepartmentId(d);

            BigDecimal dAvg = BigDecimal.ZERO;
            if (!dSummaries.isEmpty()) {
                BigDecimal dSum = dSummaries.stream()
                        .map(AttendanceSummary::getOverallPercentage)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                dAvg = dSum.divide(BigDecimal.valueOf(dSummaries.size()), 2, RoundingMode.HALF_UP);
            }

            deptMetrics.add(new CollegeOverviewResponse.DepartmentMetricDto(
                    d, d + " Department", sCount, fCount, dAvg, "Assigned HOD"
            ));
        }

        return new CollegeOverviewResponse(
                totalStudents,
                totalFaculty,
                depts.length,
                collegeAvg,
                eligible,
                condonation,
                detained,
                deptMetrics
        );
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs(int page, int size) {
        Page<com.tecnrt.modules.hod.entity.AuditLog> logs =
                auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));

        return logs.getContent().stream()
                .map(l -> new AuditLogResponse(
                        l.getId(),
                        l.getUser() != null ? l.getUser().getUsername() : "SYSTEM",
                        l.getRole(),
                        l.getAction(),
                        l.getDetails(),
                        l.getAffectedEntity(),
                        l.getAffectedId(),
                        l.getIpAddress(),
                        l.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void assignHod(CreateHodRequest request) {
        String email = request.email().trim().toLowerCase();
        String deptId = request.departmentId().trim().toUpperCase();

        if (userRepository.existsByUsernameIgnoreCase(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists.");
        }

        User hodUser = new User();
        hodUser.setUsername(email);
        hodUser.setPasswordHash(passwordEncoder.encode(request.initialPassword()));
        hodUser.setRole("HOD");
        hodUser.setActive(true);
        hodUser.setPasswordChanged(false);

        User savedUser = userRepository.save(hodUser);

        // Delete previous primary assignment for dept if any
        hodAssignmentRepository.findByDepartmentId(deptId)
                .ifPresent(hodAssignmentRepository::delete);

        HodAssignment assignment = new HodAssignment(savedUser, deptId);
        hodAssignmentRepository.save(assignment);
    }

    @Transactional
    public void resetUserPassword(String username, String newPassword) {
        String cleanUser = username.trim();
        User user = userRepository.findByUsernameIgnoreCase(cleanUser)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + cleanUser));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(false); // require user to know/change or authenticate directly
        user.setTokenVersion(user.getTokenVersion() + 1); // invalidate any previously issued sessions
        userRepository.save(user);

        AuditLog log = new AuditLog(
                user,
                "PRINCIPAL",
                "ADMIN_RESET_PASSWORD",
                "Administrator forcibly reset password for user: " + cleanUser + " (" + user.getRole() + ")",
                "USER",
                cleanUser,
                "INTERNAL"
        );
        auditLogRepository.save(log);
    }
}

