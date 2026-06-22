package com.sbmp.inventory.product.repository;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.category.entity.Category;
import com.sbmp.inventory.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * eLoan SaaS — ProductRepository
 * Product data access layer
 */
public interface ProductRepository
        extends JpaRepository<Product, Long> {

    // ─────────────────────────────────────────────────────────────
    // SINGLE LOOKUPS
    // ─────────────────────────────────────────────────────────────

    Optional<Product> findByIdAndBusiness(
            Long id,
            Business business
    );

    Optional<Product> findBySkuAndBusiness(
            String sku,
            Business business
    );

    Optional<Product> findByBarcodeAndBusiness(
            String barcode,
            Business business
    );

    // ─────────────────────────────────────────────────────────────
    // EXISTENCE CHECKS
    // ─────────────────────────────────────────────────────────────

    boolean existsBySkuIgnoreCaseAndBusiness(
            String sku,
            Business business
    );

    boolean existsByBarcodeAndBusiness(
            String barcode,
            Business business
    );

    boolean existsBySkuIgnoreCaseAndIdNotAndBusiness(
            String sku,
            Long id,
            Business business
    );

    boolean existsByBarcodeAndIdNotAndBusiness(
            String barcode,
            Long id,
            Business business
    );

    // ─────────────────────────────────────────────────────────────
    // BUSINESS FILTER
    // ─────────────────────────────────────────────────────────────

    @EntityGraph(attributePaths = {
            "category"
    })
    Page<Product> findByBusiness(
            Business business,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "category"
    })
    List<Product> findByBusiness(
            Business business
    );

    // ─────────────────────────────────────────────────────────────
    // CATEGORY FILTER
    // ─────────────────────────────────────────────────────────────

    @EntityGraph(attributePaths = {
            "category"
    })
    Page<Product> findByBusinessAndCategory(
            Business business,
            Category category,
            Pageable pageable
    );

    List<Product>
    findByBusinessAndCategoryAndActiveTrue(
            Business business,
            Category category
    );

    // ─────────────────────────────────────────────────────────────
    // ACTIVE FILTER
    // ─────────────────────────────────────────────────────────────

    @EntityGraph(attributePaths = {
            "category"
    })
    Page<Product> findByBusinessAndActive(
            Business business,
            Boolean active,
            Pageable pageable
    );

    List<Product> findByBusinessAndActiveTrue(
            Business business
    );

    // ─────────────────────────────────────────────────────────────
    // LOW STOCK
    // ─────────────────────────────────────────────────────────────

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.business = :business
            AND p.stockQuantity <= p.minimumStockAlert
            """)
    List<Product> findLowStockProducts(

            @Param("business")
            Business business
    );

    // ─────────────────────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────────────────────

    @EntityGraph(attributePaths = {
            "category"
    })
    @Query("""
            SELECT p
            FROM Product p
            WHERE p.business = :business
            AND (
                    :keyword IS NULL
                    OR :keyword = ''
                    OR LOWER(p.name)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(p.sku, ''))
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(p.barcode, ''))
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            ORDER BY p.createdAt DESC
            """)
    Page<Product> search(

            @Param("business")
            Business business,

            @Param("keyword")
            String keyword,

            Pageable pageable
    );

    // ─────────────────────────────────────────────────────────────
    // COUNTS
    // ─────────────────────────────────────────────────────────────

    long countByBusiness(
            Business business
    );

    long countByBusinessAndActive(
            Business business,
            Boolean active
    );

    long countByBusinessAndStockQuantityLessThanEqual(
            Business business,
            Integer stock
    );
}