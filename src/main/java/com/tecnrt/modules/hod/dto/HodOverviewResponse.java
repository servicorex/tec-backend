package com.tecnrt.modules.hod.dto;

import java.math.BigDecimal;
import java.util.List;

public record HodOverviewResponse(
        String departmentId,
        String departmentName,
        long totalStudents,
        long totalFaculty,
        BigDecimal averageAttendance,
        long eligibleCount,
        long condonationCount,
        long detainedCount,
        List<SectionStatDto> sections
) {
    public record SectionStatDto(
            String year,
            String section,
            long studentCount,
            BigDecimal attendancePercentage
    ) {}
}

