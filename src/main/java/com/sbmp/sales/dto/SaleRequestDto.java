package com.sbmp.sales.dto;

import com.sbmp.sales.enums.PaymentMethod;
import com.sbmp.sales.enums.SaleStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SaleRequestDto {

    @NotNull
    private Long customerId;

    @NotNull
    private Long businessId;

    private LocalDate saleDate;

    private String notes;

    private BigDecimal advancePaid;

    private PaymentMethod paymentMethod;


    private BigDecimal invoiceDiscount;

    private SaleStatus status;


    @Valid
    private List<SaleItemRequestDto> items =
            new ArrayList<>();
}