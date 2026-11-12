package com.tecnrt.modules.hod.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SectionResponse(
        UUID id,
        String departmentId,
        String sectionName,
        String academicYear,
        boolean active,
        OffsetDateTime createdAt
) {}

