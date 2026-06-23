package com.sbmp.sales.repository;

import com.sbmp.business.entity.Business;
import com.sbmp.customer.entity.Customer;
import com.sbmp.sales.entity.Sale;
import com.sbmp.sales.enums.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaleRepository
        extends JpaRepository<Sale, Long> {

    Optional<Sale> findByIdAndBusiness(
            Long id,
            Business business
    );

    Optional<Sale> findByInvoiceNo(
            String invoiceNo
    );

    boolean existsByInvoiceNo(
            String invoiceNo
    );

    Page<Sale> findByBusiness(
            Business business,
            Pageable pageable
    );

    Page<Sale> findByBusinessAndStatus(
            Business business,
            SaleStatus status,
            Pageable pageable
    );

    Page<Sale> findByBusinessAndCustomer(
            Business business,
            Customer customer,
            Pageable pageable
    );

    Page<Sale> findByBusinessAndSaleDateBetween(
            Business business,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    long countByBusiness(
            Business business
    );

    long countByInvoiceNoStartingWith(String prefix);

    // ─────────────────────────────────────────────
    // Dashboard / Controller-এর জন্য নতুন মেথড (by businessId)
    // ─────────────────────────────────────────────

    long countByBusinessId(Long businessId);

    List<Sale> findByBusinessIdOrderBySaleDateDesc(Long businessId);

    List<Sale> findByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);

    List<Sale> findByBusinessIdAndStatusOrderByCreatedAtDesc(Long businessId, SaleStatus status);

    @Query("""
        SELECT COALESCE(SUM(s.grandTotal), 0)
        FROM Sale s
        WHERE s.business.id = :businessId
        AND s.status <> com.sbmp.sales.enums.SaleStatus.CANCELLED
    """)
    BigDecimal sumGrandTotalByBusinessId(@Param("businessId") Long businessId);

    @Query("""
        SELECT COALESCE(SUM(s.paidAmount), 0)
        FROM Sale s
        WHERE s.business.id = :businessId
        AND s.status <> com.sbmp.sales.enums.SaleStatus.CANCELLED
    """)
    BigDecimal sumPaidAmountByBusinessId(@Param("businessId") Long businessId);

    @Query("""
        SELECT COALESCE(SUM(s.dueAmount), 0)
        FROM Sale s
        WHERE s.business.id = :businessId
        AND s.status <> com.sbmp.sales.enums.SaleStatus.CANCELLED
    """)
    BigDecimal sumDueAmountByBusinessId(@Param("businessId") Long businessId);

    @Query("""
        SELECT COALESCE(SUM(s.grandTotal), 0)
        FROM Sale s
        WHERE s.business.id = :businessId
        AND s.status <> com.sbmp.sales.enums.SaleStatus.CANCELLED
        AND s.saleDate BETWEEN :from AND :to
    """)
    BigDecimal sumGrandTotalByBusinessIdAndSaleDateBetween(
            @Param("businessId") Long businessId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}