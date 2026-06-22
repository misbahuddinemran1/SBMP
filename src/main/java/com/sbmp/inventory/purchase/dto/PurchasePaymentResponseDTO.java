package com.sbmp.inventory.purchase.dto;

import com.sbmp.inventory.purchase.entity.PurchasePayment.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasePaymentResponseDTO {

    private Long id;

    private PaymentMethod paymentMethod;

    private BigDecimal amount;

    private BigDecimal advanceAmount;

    private String referenceNo;

    private String accountInfo;

    private LocalDate paymentDate;

    private String notes;
}