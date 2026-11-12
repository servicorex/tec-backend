package com.tecnrt.modules.hod.dto;

import java.math.BigDecimal;
import java.util.List;

public record AttendanceValidationResponse(
        boolean isValid,
        ValidationSummary summary,
        List<ValidatedRow> rows,
        String generalError
) {
    public record ValidationSummary(
            int totalRows,
            int validRows,
            int rejectedRows,
            int warningRows
    ) {}

    public record ValidatedRow(
            int rowNumber,
            String rollNumber,
            String studentName,
            String subjectCode,
            int totalClassesHeld,
            int classesAttended,
            int classesAbsent,
            BigDecimal attendancePercentage,
            String status, // 'VALID', 'WARNING', 'REJECTED'
            List<String> errors,
            List<String> warnings
    ) {}
}

