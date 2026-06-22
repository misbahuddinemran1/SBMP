package com.sbmp.inventory.stock.repository;

import com.sbmp.inventory.stock.entity.StockTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockTransactionRepository
        extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction>
    findByProduct_IdAndBusiness_IdOrderByCreatedAtDesc(
            Long productId,
            Long businessId
    );

    Page<StockTransaction>
    findByBusiness_IdOrderByCreatedAtDesc(
            Long businessId,
            Pageable pageable
    );

    List<StockTransaction>
    findByReferenceIdAndReferenceType(
            Long referenceId,
            String referenceType
    );

    @Query("""
            SELECT t
            FROM StockTransaction t
            WHERE t.business.id = :businessId
            AND t.createdAt BETWEEN :from AND :to
            ORDER BY t.createdAt DESC
            """)
    List<StockTransaction> findByBusinessAndDateRange(
            @Param("businessId") Long businessId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    List<StockTransaction>
    findByBusiness_IdAndTransactionTypeOrderByCreatedAtDesc(
            Long businessId,
            StockTransaction.TransactionType transactionType
    );

    Optional<StockTransaction>
    findTopByProduct_IdAndBusiness_IdOrderByCreatedAtDesc(
            Long productId,
            Long businessId
    );
}