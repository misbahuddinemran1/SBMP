package com.sbmp.inventory.purchase.dto;

import com.sbmp.inventory.purchase.entity.PurchasePayment.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasePaymentRequestDTO {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private BigDecimal amount;

    @DecimalMin(value = "0.00", message = "Advance amount cannot be negative")
    private BigDecimal advanceAmount;

    @Size(max = 100)
    private String referenceNo;

    @Size(max = 100)
    private String accountInfo;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @Size(max = 255)
    private String notes;
}