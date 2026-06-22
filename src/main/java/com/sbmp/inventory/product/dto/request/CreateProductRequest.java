package com.sbmp.inventory.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * eLoan SaaS — CreateProductRequest
 */
@Getter
@Setter
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 150)
    private String name;

    private String sku;

    private String barcode;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0")
    private BigDecimal purchasePrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0")
    private BigDecimal sellingPrice;

    private Integer stockQuantity = 0;

    private Integer minimumStockAlert = 5;

    private String unit;

    private Boolean active = true;

    @NotNull(message = "Category is required")
    private Long categoryId;
}