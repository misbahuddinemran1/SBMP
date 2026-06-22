package com.sbmp.inventory.purchase.repository;

import com.sbmp.inventory.purchase.entity.PurchasePayment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PurchasePaymentRepository extends JpaRepository<PurchasePayment, Long> {

    List<PurchasePayment> findAllByPurchaseId(Long purchaseId);

    @Query("""
            SELECT COALESCE(SUM(pp.amount), 0)
            FROM PurchasePayment pp
            WHERE pp.purchase.id = :purchaseId
            """)
    BigDecimal sumAmountByPurchaseId(
            @Param("purchaseId") Long purchaseId
    );
}