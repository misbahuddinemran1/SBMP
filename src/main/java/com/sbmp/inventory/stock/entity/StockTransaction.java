// StockTransaction.java
package com.sbmp.inventory.stock.entity;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(
        name = "stock_transactions",
        indexes = {
                @Index(
                        name = "idx_stock_tx_product",
                        columnList = "product_id"
                ),
                @Index(
                        name = "idx_stock_tx_business",
                        columnList = "business_id"
                ),
                @Index(
                        name = "idx_stock_tx_reference",
                        columnList = "reference_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─────────────────────────────────────────────
    // TRANSACTION TYPE
    // ─────────────────────────────────────────────

    public enum TransactionType {

        PURCHASE_IN,

        PURCHASE_CANCEL,

        SALE_OUT,

        SALE_CANCEL,

        SALE_RETURN,

        ADJUSTMENT
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType transactionType;

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
    // STOCK SNAPSHOT
    // ─────────────────────────────────────────────

    @Column(nullable = false)
    private Integer quantityBefore;

    @Column(nullable = false)
    private Integer quantityChanged;

    @Column(nullable = false)
    private Integer quantityAfter;

    // ─────────────────────────────────────────────
    // DOCUMENT REFERENCE
    // ─────────────────────────────────────────────

    @Column(length = 50)
    private String referenceType;

    @Column(length = 50)
    private String referenceNumber;

    private Long referenceId;

    // ─────────────────────────────────────────────
    // COSTING
    // ─────────────────────────────────────────────

    @Column(
            precision = 12,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(
            precision = 12,
            scale = 2
    )
    private BigDecimal averageCost;

    // ─────────────────────────────────────────────
    // NOTES
    // ─────────────────────────────────────────────

    @Column(length = 500)
    private String notes;

    // ─────────────────────────────────────────────
    // AUDIT
    // ─────────────────────────────────────────────

    @CreationTimestamp
    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}