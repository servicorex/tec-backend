package com.tecnrt.modules.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record CollegeOverviewResponse(
        long totalStudents,
        long totalFaculty,
        long totalDepartments,
        BigDecimal collegeAttendanceAverage,
        long totalEligible,
        long totalCondonation,
        long totalDetained,
        List<DepartmentMetricDto> departmentMetrics
) {
    public record DepartmentMetricDto(
            String departmentId,
            String departmentName,
            long studentCount,
            long facultyCount,
            BigDecimal attendancePercentage,
            String hodName
    ) {}
}

