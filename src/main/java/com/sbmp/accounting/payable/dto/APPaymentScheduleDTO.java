package com.sbmp.accounting.payable.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class APPaymentScheduleDTO {

    private Long id;
    private Long apId;
    private LocalDate scheduledDate;
    private BigDecimal scheduledAmount;
    private BigDecimal paidAmount;
    private String status;
    private String paymentReference;
    private String notes;
}