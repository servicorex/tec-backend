package com.tecnrt.modules.student.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentFeesResponse(
        String academicYear,
        String studentName,
        String rollNumber,
        String department,
        BigDecimal totalAnnualFee,
        BigDecimal paidAmount,
        BigDecimal dueAmount,
        String advisoryNotice,
        List<FeeItemDto> feeItems,
        List<FeeTransactionDto> transactions
) {
    public record FeeItemDto(
            String id,
            String name,
            BigDecimal amount,
            BigDecimal paid,
            BigDecimal balanceDue,
            String dueDate,
            String status
    ) {}

    public record FeeTransactionDto(
            String transactionRef,
            String dateTime,
            String feeCategory,
            BigDecimal amountPaid,
            String paymentMode,
            String receiptNo,
            String status
    ) {}
}

