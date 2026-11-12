package com.tecnrt.modules.hod.dto;

import java.util.List;

public record ProvisionResultResponse(
        int totalRequested,
        int successfullyProvisioned,
        int alreadyProvisioned,
        int failed,
        List<ProvisionDetail> details
) {
    public record ProvisionDetail(
            String rollNumber,
            String status, // 'PROVISIONED', 'ALREADY_EXISTS', 'NOT_FOUND'
            String message
    ) {}
}

