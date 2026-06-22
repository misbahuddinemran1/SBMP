package com.sbmp.sales.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SaleItemResponseDto {

    private Long id;

    private Long productId;

    private String productName;

    private String categoryName;

    private Integer stockBefore;

    private Integer stockAfter;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal discountAmount;

    private BigDecimal discountPercentage;

    private BigDecimal itemTotal;
}