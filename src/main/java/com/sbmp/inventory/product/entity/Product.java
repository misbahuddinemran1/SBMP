package com.sbmp.inventory.product.entity;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.category.entity.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * eLoan SaaS — Product Entity
 * Inventory Product Model
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─────────────────────────────────────────────────────────────
    // BASIC INFORMATION
    // ─────────────────────────────────────────────────────────────

    @NotBlank(message = "Product name is required")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String sku;

    @Column(length = 100)
    private String barcode;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    // ─────────────────────────────────────────────────────────────
    // PRICING
    // ─────────────────────────────────────────────────────────────

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;
    
    private BigDecimal lastPurchasePrice;

    // ─────────────────────────────────────────────────────────────
    // STOCK
    // ─────────────────────────────────────────────────────────────

    @Builder.Default
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer minimumStockAlert = 5;

    @Column(length = 30)
    private String unit;

    // ─────────────────────────────────────────────────────────────
    // STATUS
    // ─────────────────────────────────────────────────────────────

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // ─────────────────────────────────────────────────────────────
    // RELATIONSHIPS
    // ─────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",
            nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id",
            nullable = false)
    private Business business;

    // ─────────────────────────────────────────────────────────────
    // AUDIT
    // ─────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false,
            updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    public boolean isLowStock() {

        return stockQuantity != null
                && minimumStockAlert != null
                && stockQuantity <= minimumStockAlert;
    }

    public boolean isOutOfStock() {

        return stockQuantity != null
                && stockQuantity <= 0;
    }
}