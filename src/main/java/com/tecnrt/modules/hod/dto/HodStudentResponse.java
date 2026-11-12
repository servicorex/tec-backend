package com.tecnrt.modules.hod.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record HodStudentResponse(
        UUID id,
        String rollNumber,
        String fullName,
        String departmentId,
        String year,
        String semester,
        String section,
        boolean lateralEntry,
        String status,
        String accountStatus,
        String profileStatus,
        int profileCompletion,
        BigDecimal cgpa,
        boolean hasUserAccount
) {}

