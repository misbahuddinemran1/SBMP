package com.sbmp.accounting.payable.service;

import com.sbmp.accounting.payable.dto.*;
import com.sbmp.accounting.payable.entity.AccountsPayable.APStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface APService {

    // Create/Update AP
    AccountsPayableDTO createAP(Long purchaseId, String invoiceNumber, LocalDate dueDate);
    AccountsPayableDTO updateAPStatus(Long apId, APStatus newStatus);

    // Get AP details
    AccountsPayableDTO getAPById(Long apId);
    Page<AccountsPayableDTO> searchAP(
            Long businessId,
            APStatus status,
            Long supplierId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    // Payment operations
    AccountsPayableDTO recordPayment(Long apId, BigDecimal amount, String paymentReference);
    AccountsPayableDTO recordPartialPayment(Long apId, BigDecimal amount);

    // AP Aging Report
    List<APAgingReportDTO> getAPAgingReport(Long businessId);

    // Supplier Outstanding Summary
    List<SupplierOutstandingDTO> getSupplierOutstandingSummary(Long businessId);

    // Dashboard metrics
    BigDecimal getTotalOutstandingAmount(Long businessId);
    List<AccountsPayableDTO> getOverdueAP(Long businessId);

    // Payment Schedule
    List<APPaymentScheduleDTO> getPaymentSchedules(Long apId);
    APPaymentScheduleDTO addPaymentSchedule(
            Long apId,
            LocalDate scheduledDate,
            BigDecimal scheduledAmount
    );
}