package com.sbmp.inventory.product.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * eLoan SaaS — ProductResponse
 */
@Getter
@Builder
public class ProductResponse {

    private Long id;

    private String name;

    private String sku;

    private String barcode;

    private String description;

    private BigDecimal purchasePrice;

    private BigDecimal sellingPrice;

    private Integer stockQuantity;

    private Integer minimumStockAlert;

    private String unit;

    private Boolean active;

    private Boolean lowStock;

    private Boolean outOfStock;

    private Long categoryId;

    private String categoryName;

    private Long businessId;

    private String businessName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}