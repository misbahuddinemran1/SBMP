package com.sbmp.inventory.purchase.dto;

import com.sbmp.inventory.purchase.entity.Purchase.PurchaseStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseSummaryDTO {

    private Long id;

    private String purchaseNumber;

    private String supplierName;

    private LocalDate purchaseDate;

    private PurchaseStatus status;

    private BigDecimal grandTotal;

    private BigDecimal paidAmount;

    private BigDecimal advancePaid;

    private BigDecimal dueAmount;

    private LocalDateTime createdAt;
}