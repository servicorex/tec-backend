package com.tecnrt.modules.admin.controller;

import com.tecnrt.modules.admin.dto.AuditLogResponse;
import com.tecnrt.modules.admin.dto.CollegeOverviewResponse;
import com.tecnrt.modules.admin.dto.CreateHodRequest;
import com.tecnrt.modules.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administrator / Principal Portal", description = "College-wide institutional oversight, HOD management, and immutable audit logs")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get College-wide Overview", description = "Aggregates student metrics, branch distribution, and total attendance KPIs across all departments")
    public ResponseEntity<CollegeOverviewResponse> getOverview() {
        return ResponseEntity.ok(adminService.getCollegeOverview());
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get System Audit Trail", description = "Returns immutable institutional audit logs for regulatory and accreditation verification")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(adminService.getAuditLogs(page, size));
    }

    @PostMapping("/hod")
    @Operation(summary = "Assign or Provision Department HOD", description = "Creates HOD credentials and establishes department head authority")
    public ResponseEntity<Map<String, Object>> assignHod(@Valid @RequestBody CreateHodRequest request) {
        adminService.assignHod(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "HOD successfully assigned for department " + request.departmentId()
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Admin Reset Password", description = "Allows Administrator to reset password for any student or HOD without current password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody com.tecnrt.modules.admin.dto.AdminResetPasswordRequest request) {
        adminService.resetUserPassword(request.username(), request.newPassword());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password for user " + request.username() + " has been reset successfully."
        ));
    }
}

