package com.sbmp.accounting.payable.dto;

import com.sbmp.accounting.payable.entity.AccountsPayable.APStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsPayableDTO {

    private Long id;
    private Long purchaseId;
    private Long supplierId;
    private String supplierName;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal invoiceAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private APStatus status;
    private Integer paymentTermsDays;
    private BigDecimal earlyPaymentDiscount;
    private Long daysOverdue;
    private Boolean isOverdue;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}