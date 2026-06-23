package com.sbmp.accounting.payable.service.impl;

import com.sbmp.accounting.payable.dto.*;
import com.sbmp.accounting.payable.entity.AccountsPayable;
import com.sbmp.accounting.payable.entity.AccountsPayable.APStatus;
import com.sbmp.accounting.payable.entity.APPaymentSchedule;
import com.sbmp.accounting.payable.repository.AccountsPayableRepository;
import com.sbmp.accounting.payable.repository.APPaymentScheduleRepository;
import com.sbmp.inventory.purchase.entity.Purchase;
import com.sbmp.inventory.purchase.repository.PurchaseRepository;
import com.sbmp.accounting.payable.service.APService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class APServiceImpl implements APService {

    private final AccountsPayableRepository apRepository;
    private final APPaymentScheduleRepository scheduleRepository;
    private final PurchaseRepository purchaseRepository;

    @Override
    public AccountsPayableDTO createAP(Long purchaseId, String invoiceNumber, LocalDate dueDate) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        // Check if AP already exists for this purchase
        Optional<AccountsPayable> existingAP = apRepository.findByPurchaseId(purchaseId);
        if (existingAP.isPresent()) {
            throw new RuntimeException("AP already exists for this purchase");
        }

        AccountsPayable ap = AccountsPayable.builder()
                .purchase(purchase)
                .supplier(purchase.getSupplier())
                .business(purchase.getBusiness())
                .invoiceNumber(invoiceNumber)
                .invoiceDate(LocalDate.now())
                .dueDate(dueDate)
                .invoiceAmount(purchase.getGrandTotal())
                .outstandingAmount(purchase.getGrandTotal())
                .paidAmount(BigDecimal.ZERO)
                .status(APStatus.PENDING)
                .paymentTermsDays((int) ChronoUnit.DAYS.between(LocalDate.now(), dueDate))
                .build();

        AccountsPayable saved = apRepository.save(ap);
        log.info("AP created for Purchase ID: {}", purchaseId);
        return mapToDTO(saved);
    }

    @Override
    public AccountsPayableDTO recordPayment(Long apId, BigDecimal amount, String paymentReference) {
        AccountsPayable ap = apRepository.findById(apId)
                .orElseThrow(() -> new RuntimeException("AP not found"));

        BigDecimal newPaidAmount = ap.getPaidAmount().add(amount);
        BigDecimal newOutstandingAmount = ap.getInvoiceAmount().subtract(newPaidAmount);

        // Determine status
        APStatus newStatus;
        if (newOutstandingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            newStatus = APStatus.PAID;
            newOutstandingAmount = BigDecimal.ZERO;
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            newStatus = APStatus.PARTIALLY_PAID;
        } else {
            newStatus = ap.isOverdue() ? APStatus.OVERDUE : APStatus.PENDING;
        }

        ap.setPaidAmount(newPaidAmount);
        ap.setOutstandingAmount(newOutstandingAmount);
        ap.setStatus(newStatus);

        AccountsPayable updated = apRepository.save(ap);
        log.info("Payment recorded for AP ID: {}, Amount: {}", apId, amount);
        return mapToDTO(updated);
    }

    @Override
    public AccountsPayableDTO recordPartialPayment(Long apId, BigDecimal amount) {
        return recordPayment(apId, amount, null);
    }

    @Override
    public AccountsPayableDTO updateAPStatus(Long apId, APStatus newStatus) {
        AccountsPayable ap = apRepository.findById(apId)
                .orElseThrow(() -> new RuntimeException("AP not found"));

        ap.setStatus(newStatus);
        AccountsPayable updated = apRepository.save(ap);
        log.info("AP Status updated to {} for AP ID: {}", newStatus, apId);
        return mapToDTO(updated);
    }

    @Override
    public AccountsPayableDTO getAPById(Long apId) {
        AccountsPayable ap = apRepository.findById(apId)
                .orElseThrow(() -> new RuntimeException("AP not found"));
        return mapToDTO(ap);
    }

    @Override
    public Page<AccountsPayableDTO> searchAP(Long businessId, APStatus status, Long supplierId,
                                             LocalDate from, LocalDate to, Pageable pageable) {
        return apRepository.searchAP(businessId, status, supplierId, from, to, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public List<APAgingReportDTO> getAPAgingReport(Long businessId) {
        List<AccountsPayable> apList = apRepository.getAPAgingData(businessId);

        Map<Long, APAgingReportDTO> reportMap = new LinkedHashMap<>();

        for (AccountsPayable ap : apList) {
            Long supplierId = ap.getSupplier().getId();
            APAgingReportDTO report = reportMap.computeIfAbsent(supplierId, k ->
                    APAgingReportDTO.builder()
                            .supplierId(supplierId)
                            .supplierName(ap.getSupplier().getSupplierName())
                            .days0To30(BigDecimal.ZERO)
                            .days30To60(BigDecimal.ZERO)
                            .days60To90(BigDecimal.ZERO)
                            .days90Plus(BigDecimal.ZERO)
                            .totalOutstanding(BigDecimal.ZERO)
                            .invoiceCount(0)
                            .build()
            );

            long daysOverdue = ap.getDaysOverdue();
            BigDecimal outstanding = ap.getOutstandingAmount();

            if (daysOverdue <= 30) {
                report.setDays0To30(report.getDays0To30().add(outstanding));
            } else if (daysOverdue <= 60) {
                report.setDays30To60(report.getDays30To60().add(outstanding));
            } else if (daysOverdue <= 90) {
                report.setDays60To90(report.getDays60To90().add(outstanding));
            } else {
                report.setDays90Plus(report.getDays90Plus().add(outstanding));
            }

            report.setTotalOutstanding(report.getTotalOutstanding().add(outstanding));
            report.setInvoiceCount(report.getInvoiceCount() + 1);
        }

        // Calculate percentages
        BigDecimal grandTotal = reportMap.values().stream()
                .map(APAgingReportDTO::getTotalOutstanding)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        reportMap.values().forEach(report -> {
            if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = report.getTotalOutstanding()
                        .divide(grandTotal, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                report.setPercentageOfTotal(percentage);
            } else {
                report.setPercentageOfTotal(BigDecimal.ZERO);
            }
        });

        return new ArrayList<>(reportMap.values());
    }

    @Override
    public List<SupplierOutstandingDTO> getSupplierOutstandingSummary(Long businessId) {
        List<Object[]> results = apRepository.getOutstandingBySupplier(businessId);

        return results.stream().map(row -> SupplierOutstandingDTO.builder()
                .supplierName((String) row[0])
                .outstandingAmount((BigDecimal) row[1])
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalOutstandingAmount(Long businessId) {
        return apRepository.getTotalOutstanding(businessId);
    }

    @Override
    public List<AccountsPayableDTO> getOverdueAP(Long businessId) {
        return apRepository.findOverdueAP(businessId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<APPaymentScheduleDTO> getPaymentSchedules(Long apId) {
        return scheduleRepository.findSchedulesByAPId(apId).stream()
                .map(this::mapScheduleToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public APPaymentScheduleDTO addPaymentSchedule(Long apId, LocalDate scheduledDate, BigDecimal scheduledAmount) {
        AccountsPayable ap = apRepository.findById(apId)
                .orElseThrow(() -> new RuntimeException("AP not found"));

        APPaymentSchedule schedule = APPaymentSchedule.builder()
                .accountsPayable(ap)
                .scheduledDate(scheduledDate)
                .scheduledAmount(scheduledAmount)
                .paidAmount(BigDecimal.ZERO)
                .build();

        APPaymentSchedule saved = scheduleRepository.save(schedule);
        ap.addPaymentSchedule(saved);
        apRepository.save(ap);

        log.info("Payment schedule added for AP ID: {}", apId);
        return mapScheduleToDTO(saved);
    }

    // Helper methods
    private AccountsPayableDTO mapToDTO(AccountsPayable ap) {
        return AccountsPayableDTO.builder()
                .id(ap.getId())
                .purchaseId(ap.getPurchase().getId())
                .supplierId(ap.getSupplier().getId())
                .supplierName(ap.getSupplier().getSupplierName())
                .invoiceNumber(ap.getInvoiceNumber())
                .invoiceDate(ap.getInvoiceDate())
                .dueDate(ap.getDueDate())
                .invoiceAmount(ap.getInvoiceAmount())
                .paidAmount(ap.getPaidAmount())
                .outstandingAmount(ap.getOutstandingAmount())
                .status(ap.getStatus())
                .paymentTermsDays(ap.getPaymentTermsDays())
                .earlyPaymentDiscount(ap.getEarlyPaymentDiscount())
                .daysOverdue(ap.getDaysOverdue())
                .isOverdue(ap.isOverdue())
                .notes(ap.getNotes())
                .createdAt(ap.getCreatedAt())
                .updatedAt(ap.getUpdatedAt())
                .build();
    }

    private APPaymentScheduleDTO mapScheduleToDTO(APPaymentSchedule schedule) {
        return APPaymentScheduleDTO.builder()
                .id(schedule.getId())
                .apId(schedule.getAccountsPayable().getId())
                .scheduledDate(schedule.getScheduledDate())
                .scheduledAmount(schedule.getScheduledAmount())
                .paidAmount(schedule.getPaidAmount())
                .status(schedule.getStatus().toString())
                .paymentReference(schedule.getPaymentReference())
                .notes(schedule.getNotes())
                .build();
    }
}