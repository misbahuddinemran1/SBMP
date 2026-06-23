package com.sbmp.sales.repository;

import com.sbmp.sales.entity.Sale;
import com.sbmp.sales.entity.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaleItemRepository
        extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findBySale(
            Sale sale
    );

    void deleteBySale(
            Sale sale
    );

    // ─────────────────────────────────────────────
    // Top selling products (dashboard chart-এর জন্য)
    // ─────────────────────────────────────────────
    @Query("""
        SELECT si.product.name, SUM(si.quantity)
        FROM SaleItem si
        WHERE si.sale.business.id = :businessId
        AND si.sale.status <> com.sbmp.sales.enums.SaleStatus.CANCELLED
        GROUP BY si.product.name
        ORDER BY SUM(si.quantity) DESC
    """)
    List<Object[]> findTopProductsByBusinessId(
            @Param("businessId") Long businessId,
            Pageable pageable
    );
}