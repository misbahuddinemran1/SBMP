package com.sbmp.sales.dto;

import com.sbmp.sales.enums.PaymentStatus;
import com.sbmp.sales.enums.SaleStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SaleResponseDto {

    private Long id;

    private String invoiceNo;

    private Long customerId;

    private String customerName;

private String customerPhone;

    private Long businessId;

    private String businessName;

    private LocalDate saleDate;

    private SaleStatus status;

    private PaymentStatus paymentStatus;

    private BigDecimal subtotal;

    private BigDecimal totalDiscount;

    private BigDecimal invoiceDiscount;

    private BigDecimal grandTotal;

    private BigDecimal paidAmount;

    private BigDecimal dueAmount;

    private BigDecimal advancePaid;

    private String notes;

    private List<SaleItemResponseDto> items;

    private List<SalePaymentResponseDto> payments;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}