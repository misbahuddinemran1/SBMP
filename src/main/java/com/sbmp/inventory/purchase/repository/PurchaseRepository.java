package com.sbmp.inventory.purchase.repository;

import com.sbmp.inventory.purchase.entity.Purchase;
import com.sbmp.inventory.purchase.entity.Purchase.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Optional<Purchase> findByPurchaseNumber(String purchaseNumber);

    List<Purchase> findByBusiness_IdOrderByCreatedAtDesc(Long businessId);

    List<Purchase> findByBusiness_IdAndStatusOrderByCreatedAtDesc(
            Long businessId,
            Purchase.PurchaseStatus status
    );

    @Query("SELECT p FROM Purchase p WHERE p.business.id = :businessId " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:supplierId IS NULL OR p.supplier.id = :supplierId) " +
           "AND (:from IS NULL OR p.purchaseDate >= :from) " +
           "AND (:to IS NULL OR p.purchaseDate <= :to)")
    Page<Purchase> searchPurchases(
            @Param("businessId") Long businessId,
            @Param("status") PurchaseStatus status,
            @Param("supplierId") Long supplierId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    // Monthly Purchase Trend (last 6 months)
    @Query("""
        SELECT FUNCTION('MONTH', p.purchaseDate),
               FUNCTION('YEAR', p.purchaseDate),
               COALESCE(SUM(p.grandTotal), 0)
        FROM Purchase p
        WHERE p.business.id = :businessId
        AND p.status = com.sbmp.inventory.purchase.entity.Purchase.PurchaseStatus.COMPLETED
        AND p.purchaseDate >= :fromDate
        GROUP BY FUNCTION('YEAR', p.purchaseDate), FUNCTION('MONTH', p.purchaseDate)
        ORDER BY FUNCTION('YEAR', p.purchaseDate), FUNCTION('MONTH', p.purchaseDate)
        """)
    List<Object[]> getMonthlyPurchaseTrend(
            @Param("businessId") Long businessId,
            @Param("fromDate") LocalDate fromDate
    );

    // Payment Method Breakdown
    @Query("""
        SELECT pp.paymentMethod, COALESCE(SUM(pp.amount), 0)
        FROM PurchasePayment pp
        WHERE pp.purchase.business.id = :businessId
        GROUP BY pp.paymentMethod
        """)
    List<Object[]> getPaymentMethodBreakdown(
            @Param("businessId") Long businessId
    );

    // Top 5 Suppliers — Pageable দিয়ে LIMIT করা হয়েছে
    @Query("""
        SELECT p.supplier.supplierName, COALESCE(SUM(p.grandTotal), 0)
        FROM Purchase p
        WHERE p.business.id = :businessId
        AND p.status = com.sbmp.inventory.purchase.entity.Purchase.PurchaseStatus.COMPLETED
        GROUP BY p.supplier.id, p.supplier.supplierName
        ORDER BY SUM(p.grandTotal) DESC
        """)
    List<Object[]> getTopSuppliers(
            @Param("businessId") Long businessId,
            Pageable pageable
    );

    // Top 5 Products — Pageable দিয়ে LIMIT করা হয়েছে
    @Query("""
        SELECT pi.product.name, COALESCE(SUM(pi.purchaseQty), 0)
        FROM PurchaseItem pi
        WHERE pi.purchase.business.id = :businessId
        AND pi.purchase.status = com.sbmp.inventory.purchase.entity.Purchase.PurchaseStatus.COMPLETED
        GROUP BY pi.product.id, pi.product.name
        ORDER BY SUM(pi.purchaseQty) DESC
        """)
    List<Object[]> getTopProducts(
            @Param("businessId") Long businessId,
            Pageable pageable
    );

    // This Month vs Last Month
    @Query("""
        SELECT COALESCE(SUM(p.grandTotal), 0)
        FROM Purchase p
        WHERE p.business.id = :businessId
        AND p.status = com.sbmp.inventory.purchase.entity.Purchase.PurchaseStatus.COMPLETED
        AND p.purchaseDate >= :fromDate
        AND p.purchaseDate <= :toDate
        """)
    BigDecimal getPurchaseTotalBetween(
            @Param("businessId") Long businessId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
