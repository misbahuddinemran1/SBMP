package com.sbmp.accounting.payable.repository;

import com.sbmp.accounting.payable.entity.AccountsPayable;
import com.sbmp.accounting.payable.entity.AccountsPayable.APStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountsPayableRepository extends JpaRepository<AccountsPayable, Long> {

    // Find AP by purchase
    Optional<AccountsPayable> findByPurchaseId(Long purchaseId);

    // Find all AP for a supplier
    List<AccountsPayable> findBySupplierIdAndBusinessIdOrderByDueDateDesc(
            Long supplierId,
            Long businessId
    );

    // Find all AP for a business with filters
    @Query("""
        SELECT ap FROM AccountsPayable ap
        WHERE ap.business.id = :businessId
        AND (:status IS NULL OR ap.status = :status)
        AND (:supplierId IS NULL OR ap.supplier.id = :supplierId)
        AND (:from IS NULL OR ap.dueDate >= :from)
        AND (:to IS NULL OR ap.dueDate <= :to)
        ORDER BY ap.dueDate ASC
    """)
    Page<AccountsPayable> searchAP(
            @Param("businessId") Long businessId,
            @Param("status") APStatus status,
            @Param("supplierId") Long supplierId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    // Get overdue AP
    @Query("""
        SELECT ap FROM AccountsPayable ap
        WHERE ap.business.id = :businessId
        AND ap.dueDate < CURRENT_DATE
        AND ap.status != 'PAID'
        ORDER BY ap.dueDate ASC
    """)
    List<AccountsPayable> findOverdueAP(
            @Param("businessId") Long businessId
    );

    // Get total outstanding amount
    @Query("""
        SELECT COALESCE(SUM(ap.outstandingAmount), 0)
        FROM AccountsPayable ap
        WHERE ap.business.id = :businessId
        AND ap.status != 'CANCELLED'
        AND ap.status != 'PAID'
    """)
    BigDecimal getTotalOutstanding(
            @Param("businessId") Long businessId
    );

    // Get total outstanding by supplier
    @Query("""
        SELECT ap.supplier.supplierName, COALESCE(SUM(ap.outstandingAmount), 0)
        FROM AccountsPayable ap
        WHERE ap.business.id = :businessId
        AND ap.status != 'CANCELLED'
        AND ap.status != 'PAID'
        GROUP BY ap.supplier.id, ap.supplier.supplierName
        ORDER BY SUM(ap.outstandingAmount) DESC
    """)
    List<Object[]> getOutstandingBySupplier(
            @Param("businessId") Long businessId
    );

    // Get AP aging report (30, 60, 90+ days)
    @Query("""
        SELECT ap FROM AccountsPayable ap
        WHERE ap.business.id = :businessId
        AND ap.status != 'PAID'
        AND ap.status != 'CANCELLED'
        ORDER BY ap.dueDate ASC
    """)
    List<AccountsPayable> getAPAgingData(
            @Param("businessId") Long businessId
    );
}