
package com.sbmp.inventory.purchase.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItemResponseDTO {

    private Long id;

    private Long productId;
    private String productName;

    private Long categoryId;
    private String categoryName;

    private BigDecimal oldStock;

    private BigDecimal lastPurchasePrice;

    private BigDecimal purchaseQty;

    private BigDecimal unitPrice;

    private BigDecimal discountAmount;

    private BigDecimal discountPercentage;

    private BigDecimal itemTotal;
}