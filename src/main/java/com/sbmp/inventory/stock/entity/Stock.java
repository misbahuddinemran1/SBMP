package com.sbmp.inventory.stock.entity;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "stocks",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "product_id",
                                "business_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─────────────────────────────────────────────
    // RELATIONSHIPS
    // ─────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "business_id",
            nullable = false
    )
    private Business business;

    // ─────────────────────────────────────────────
    // STOCK
    // ─────────────────────────────────────────────

    @Builder.Default
    @Column(
            name = "current_quantity",
            nullable = false
    )
    private Integer currentQuantity = 0;

    @Builder.Default
    @Column(
            name = "minimum_stock_alert",
            nullable = false
    )
    private Integer minimumStockAlert = 5;

    @Column(length = 30)
    private String unit;

    // ─────────────────────────────────────────────
    // COSTING
    // ─────────────────────────────────────────────

    @Column(
            name = "average_cost",
            precision = 12,
            scale = 2
    )
    private BigDecimal averageCost;

    @Column(
            name = "last_purchase_price",
            precision = 12,
            scale = 2
    )
    private BigDecimal lastPurchasePrice;

    // ─────────────────────────────────────────────
    // AUDIT
    // ─────────────────────────────────────────────

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    public boolean isLowStock() {

        return currentQuantity != null
                && minimumStockAlert != null
                && currentQuantity <= minimumStockAlert;
    }

    public boolean isOutOfStock() {

        return currentQuantity != null
                && currentQuantity <= 0;
    }

    public BigDecimal getInventoryValue() {

        if (averageCost == null
                || currentQuantity == null) {

            return BigDecimal.ZERO;
        }

        return averageCost.multiply(
                BigDecimal.valueOf(currentQuantity)
        );
    }

    public void stockIn(Integer qty) {

        if (qty == null || qty <= 0) {
            return;
        }

        this.currentQuantity =
                (this.currentQuantity == null ? 0 : this.currentQuantity)
                        + qty;
    }

    public void stockOut(Integer qty) {

        if (qty == null || qty <= 0) {
            return;
        }

        int current =
                this.currentQuantity == null
                        ? 0
                        : this.currentQuantity;

        if (current < qty) {
            throw new IllegalStateException(
                    "Insufficient stock"
            );
        }

        this.currentQuantity =
                current - qty;
    }
}