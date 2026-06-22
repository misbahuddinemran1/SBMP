package com.sbmp.inventory.purchase.dto;

import com.sbmp.inventory.purchase.entity.Purchase.PurchaseStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequestDTO {

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Business is required")
    private Long businessId;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotNull(message = "Status is required")
    private PurchaseStatus status;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @DecimalMin(value = "0.00", message = "Advance paid cannot be negative")
    private BigDecimal advancePaid = BigDecimal.ZERO;

    @NotEmpty(message = "At least one purchase item is required")
    @Valid
    private List<PurchaseItemRequestDTO> items;

    @Valid
    private List<PurchasePaymentRequestDTO> payments;
}