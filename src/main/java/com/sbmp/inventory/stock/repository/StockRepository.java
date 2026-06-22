package com.sbmp.inventory.stock.repository;

import com.sbmp.inventory.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StockRepository
        extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProduct_IdAndBusiness_Id(
            Long productId,
            Long businessId
    );

    List<Stock> findByBusiness_IdOrderByProduct_NameAsc(
            Long businessId
    );

    @Query("""
            SELECT s
            FROM Stock s
            WHERE s.business.id = :businessId
            AND s.currentQuantity <= s.minimumStockAlert
            ORDER BY s.currentQuantity ASC
            """)
    List<Stock> findLowStockByBusiness(
            @Param("businessId")
            Long businessId
    );

    @Query("""
            SELECT s
            FROM Stock s
            WHERE s.business.id = :businessId
            AND s.currentQuantity <= 0
            ORDER BY s.product.name
            """)
    List<Stock> findOutOfStockByBusiness(
            @Param("businessId")
            Long businessId
    );

    @Query("""
            SELECT COALESCE(
                SUM(
                    s.currentQuantity * s.averageCost
                ),
                0
            )
            FROM Stock s
            WHERE s.business.id = :businessId
            """)
    BigDecimal getTotalStockValue(
            @Param("businessId")
            Long businessId
    );

    long countByBusiness_Id(
            Long businessId
    );
}