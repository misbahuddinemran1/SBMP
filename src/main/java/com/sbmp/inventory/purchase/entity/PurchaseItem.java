

package com.sbmp.inventory.purchase.entity;

import com.sbmp.inventory.product.entity.Product;
import com.sbmp.inventory.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Snapshot of stock before this purchase was applied.
     * Stored for historical reference.
     */
    @Column(name = "old_stock", nullable = false, precision = 15, scale = 3)
    private BigDecimal oldStock;

    /**
     * Last recorded purchase price before this purchase.
     * Stored for historical reference.
     */
    @Column(name = "last_purchase_price", precision = 15, scale = 2)
    private BigDecimal lastPurchasePrice;

    @Column(name = "purchase_qty", nullable = false, precision = 15, scale = 3)
    private BigDecimal purchaseQty;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Fixed discount amount per item row. Mutually exclusive with discountPercentage.
     */
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    /**
     * Percentage discount per item row. Mutually exclusive with discountAmount.
     */
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "item_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal itemTotal;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Calculates item total based on qty, unit price, and applicable discount.
     * Only one discount type (amount OR percentage) should be set.
     */
    public BigDecimal calculateItemTotal() {
        if (purchaseQty == null || unitPrice == null) return BigDecimal.ZERO;

        BigDecimal subtotal = purchaseQty.multiply(unitPrice);

        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            return subtotal.subtract(discountAmount).max(BigDecimal.ZERO);
        }

        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountValue = subtotal.multiply(discountPercentage)
                    .divide(BigDecimal.valueOf(100));
            return subtotal.subtract(discountValue).max(BigDecimal.ZERO);
        }

        return subtotal;
    }

    public BigDecimal getEffectiveDiscount() {
        if (purchaseQty == null || unitPrice == null) return BigDecimal.ZERO;

        BigDecimal subtotal = purchaseQty.multiply(unitPrice);

        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            return discountAmount;
        }

        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            return subtotal.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        }

        return BigDecimal.ZERO;
    }
}
