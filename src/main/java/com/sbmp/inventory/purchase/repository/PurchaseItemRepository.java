package com.sbmp.inventory.purchase.repository;

import com.sbmp.inventory.purchase.entity.PurchaseItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {

    List<PurchaseItem> findAllByPurchaseId(Long purchaseId);

    /**
     * Duplicate product check
     */
    Optional<PurchaseItem> findByPurchaseIdAndProductId(
            Long purchaseId,
            Long productId
    );

    /**
     * Last purchase price
     */
    @Query("""
            SELECT pi.unitPrice
            FROM PurchaseItem pi
            JOIN pi.purchase p
            WHERE pi.product.id = :productId
              AND p.status = 'COMPLETED'
            ORDER BY p.purchaseDate DESC, p.id DESC
            LIMIT 1
            """)
    Optional<BigDecimal> findLastPurchasePrice(
            @Param("productId") Long productId
    );
}