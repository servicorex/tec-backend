package com.tecnrt.modules.admin.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String username,
        String role,
        String action,
        String details,
        String affectedEntity,
        String affectedId,
        String ipAddress,
        OffsetDateTime createdAt
) {}

