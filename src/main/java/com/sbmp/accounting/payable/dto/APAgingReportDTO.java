package com.sbmp.accounting.payable.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class APAgingReportDTO {

    private Long supplierId;
    private String supplierName;

    private BigDecimal days0To30;    // Due within 30 days
    private BigDecimal days30To60;   // 30-60 days overdue
    private BigDecimal days60To90;   // 60-90 days overdue
    private BigDecimal days90Plus;   // 90+ days overdue

    private BigDecimal totalOutstanding;
    private Integer invoiceCount;
    private BigDecimal percentageOfTotal;
}