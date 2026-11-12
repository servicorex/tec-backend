package com.tecnrt.modules.hod.service;

import com.tecnrt.exception.ResourceNotFoundException;
import com.tecnrt.exception.UnauthorizedException;
import com.tecnrt.modules.auth.entity.User;
import com.tecnrt.modules.auth.repository.UserRepository;
import com.tecnrt.modules.hod.dto.CreateSectionRequest;
import com.tecnrt.modules.hod.dto.CreateStudentRequest;
import com.tecnrt.modules.hod.dto.DetentionOverrideRequest;
import com.tecnrt.modules.hod.entity.AuditLog;
import com.tecnrt.modules.hod.entity.HodAssignment;
import com.tecnrt.modules.hod.entity.Section;
import com.tecnrt.modules.hod.repository.AuditLogRepository;
import com.tecnrt.modules.hod.repository.HodAssignmentRepository;
import com.tecnrt.modules.hod.repository.SectionRepository;
import com.tecnrt.modules.student.entity.Student;
import com.tecnrt.modules.student.entity.StudentProfile;
import com.tecnrt.modules.student.repository.StudentProfileRepository;
import com.tecnrt.modules.student.repository.StudentRepository;
import com.tecnrt.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class HodSecurityService {

    private final HodAssignmentRepository hodAssignmentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public HodSecurityService(
            HodAssignmentRepository hodAssignmentRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {
        this.hodAssignmentRepository = hodAssignmentRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Resolves the department strictly bound to the authenticated HOD.
     * Prevents cross-department data tampering.
     */
    public String getAuthenticatedHodDepartment() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Session is unauthenticated");
        }

        return hodAssignmentRepository.findByUserId(userId)
                .map(HodAssignment::getDepartmentId)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user is not assigned to any department as HOD"));
    }

    public User getAuthenticatedHodUser() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Session is unauthenticated");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("HOD user record not found"));
    }

    @Transactional
    public void recordAudit(String action, String details, String affectedEntity, String affectedId) {
        User user = null;
        String role = "SYSTEM";
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
            role = SecurityUtils.getCurrentUserRole();
        }

        AuditLog log = new AuditLog(
                user,
                role != null ? role : "HOD",
                action,
                details,
                affectedEntity,
                affectedId,
                "127.0.0.1"
        );
        auditLogRepository.save(log);
    }
}

