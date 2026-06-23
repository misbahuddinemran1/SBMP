package com.sbmp.sales.dto;

import com.sbmp.sales.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class SalePaymentResponseDto {

    private Long id;

    private PaymentMethod paymentMethod;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private String referenceNo;

    private String notes;
}