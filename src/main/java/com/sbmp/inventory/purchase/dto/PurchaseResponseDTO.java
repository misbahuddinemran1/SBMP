package com.sbmp.inventory.purchase.dto;

import com.sbmp.inventory.purchase.entity.Purchase.PurchaseStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseResponseDTO {

    private Long id;

    private String purchaseNumber;

    private Long supplierId;

    private String supplierName;

    private String supplierPhone;

    private Long businessId;

    private String businessName;

    private LocalDate purchaseDate;

    private PurchaseStatus status;

    private BigDecimal subtotal;

    private BigDecimal totalDiscount;

    private BigDecimal grandTotal;

    private BigDecimal paidAmount;

    private BigDecimal dueAmount;

    private String notes;

    private BigDecimal advancePaid;

    private List<PurchaseItemResponseDTO> items;

    private List<PurchasePaymentResponseDTO> payments;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}