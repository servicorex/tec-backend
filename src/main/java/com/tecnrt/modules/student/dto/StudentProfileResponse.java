package com.tecnrt.modules.student.dto;

import java.time.LocalDate;

public record StudentProfileResponse(
        // Immutable Official Academic Identifiers (Read-Only)
        String rollNumber,
        String admissionNumber,
        String department,
        String year,
        String semester,
        String section,
        String course,
        String admissionYear,
        LocalDate admissionDate,
        String admissionType,
        String admissionQuota,
        String casteCategory,
        boolean isLateralEntry,
        String status,
        String accountStatus,
        String profileStatus,
        int profileCompletion,

        // Personal Identification (Editable)
        String fullLegalName,
        String shortName,
        LocalDate dob,
        String gender,
        String bloodGroup,
        String studentMobile,
        String institutionalEmail,

        // Contact & Residential Details (Editable)
        String fatherPhone,
        String fatherEmail,
        String currentAddress,
        String permanentAddress,

        // Parent / Guardian Information (Editable)
        String fatherName,
        String fatherOccupation,
        String motherName,
        String motherOccupation,
        String guardianName,
        String guardianContact,

        // Institutional Benefits
        String scholarshipStatus,
        String feeReimbursementStatus
) {}

