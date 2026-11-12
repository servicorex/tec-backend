package com.tecnrt.modules.hod.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SubjectResponse(
        UUID id,
        String subjectCode,
        String subjectName,
        String departmentId,
        String year,
        String semester,
        String regulation,
        String subjectType,
        BigDecimal credits,
        boolean active
) {}

