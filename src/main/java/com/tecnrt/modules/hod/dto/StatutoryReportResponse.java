package com.tecnrt.modules.hod.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record StatutoryReportResponse(
        String departmentId,
        String academicYear,
        String semester,
        long totalEvaluated,
        long eligibleCount,
        long condonationCount,
        long detainedCount,
        long overriddenCount,
        List<StatutoryStudentRow> students
) {
    public record StatutoryStudentRow(
            UUID summaryId,
            String rollNumber,
            String studentName,
            String year,
            String section,
            int totalClassesHeld,
            int totalClassesAttended,
            BigDecimal overallPercentage,
            String status, // 'ELIGIBLE', 'CONDONATION', 'DETAINED', 'ELIGIBLE_BY_OVERRIDE'
            boolean isOverridden,
            String overrideReason
    ) {}
}

