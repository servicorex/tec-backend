package com.tecnrt.modules.student.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentAttendanceResponse(
        String currentSemesterCode,
        String currentSemesterName,
        boolean isLateralEntry,
        List<SemesterAttendanceTabDto> allSemesters,
        ActiveSemesterAttendanceDto activeSemester
) {
    public record SemesterAttendanceTabDto(
            String code, // '1-1', '1-2', etc.
            String label,
            String fullName,
            String year,
            boolean hasData,
            boolean isApplicable, // false for Sem 1-1 & 1-2 if lateral entry
            BigDecimal percentage,
            String status // 'ELIGIBLE', 'CONDONATION', 'DETAINED', 'NOT_APPLICABLE'
    ) {}

    public record ActiveSemesterAttendanceDto(
            int totalClassesConducted,
            int totalClassesAttended,
            int totalClassesAbsent,
            BigDecimal overallPercentage,
            String status,
            List<MonthlyAttendanceRecordDto> monthlyBreakdown
    ) {}

    public record MonthlyAttendanceRecordDto(
            String academicMonth,
            int classesConducted,
            int classesAttended,
            int classesAbsent,
            BigDecimal attendancePercentage
    ) {}
}

