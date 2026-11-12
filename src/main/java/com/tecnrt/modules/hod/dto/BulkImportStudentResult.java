package com.tecnrt.modules.hod.dto;

import java.util.List;

public record BulkImportStudentResult(
        int totalRows,
        int successfullyImported,
        int skippedCount,
        List<ImportDetail> details
) {
    public record ImportDetail(
            int rowNumber,
            String rollNumber,
            String name,
            String status, // 'IMPORTED', 'SKIPPED_DUPLICATE', 'ERROR'
            String message
    ) {}
}

