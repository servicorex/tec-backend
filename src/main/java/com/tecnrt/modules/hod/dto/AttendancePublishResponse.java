package com.tecnrt.modules.hod.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AttendancePublishResponse(
        UUID attendancePeriodId,
        String departmentId,
        String year,
        String section,
        String periodName,
        int recordsSaved,
        int summariesCalculated,
        String status,
        OffsetDateTime publishedAt
) {}

