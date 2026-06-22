package com.sbmp.inventory.product.service;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * eLoan SaaS — ProductService
 * Product business logic contract
 */
public interface ProductService {

    // ─────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────

    Product save(Product product);

    Product update(Long id,
                   Product product);

    void deleteByIdAndBusiness(Long id,
                               Business business);

    // ─────────────────────────────────────────────────────────────
    // FINDERS
    // ─────────────────────────────────────────────────────────────

    Optional<Product> findById(Long id);

    Product getById(Long id);

    Product getByIdAndBusiness(Long id,
                               Business business);

    Optional<Product> findBySku(
            String sku,
            Business business
    );

    Optional<Product> findByBarcode(
            String barcode,
            Business business
    );

    // ─────────────────────────────────────────────────────────────
    // LISTING
    // ─────────────────────────────────────────────────────────────

    Page<Product> findAll(Business business,
                          Pageable pageable);

    Page<Product> search(Business business,
                         String keyword,
                         Pageable pageable);

    List<Product> findAllActive(
            Business business
    );

    // ─────────────────────────────────────────────────────────────
    // LOW STOCK
    // ─────────────────────────────────────────────────────────────

    List<Product> getLowStockProducts(
            Business business
    );

    // ─────────────────────────────────────────────────────────────
    // VALIDATION
    // ─────────────────────────────────────────────────────────────

    boolean existsBySku(Business business,
                        String sku);

    boolean existsByBarcode(Business business,
                            String barcode);

    boolean existsBySkuAndIdNot(
            Business business,
            String sku,
            Long id
    );

    boolean existsByBarcodeAndIdNot(
            Business business,
            String barcode,
            Long id
    );

    // ─────────────────────────────────────────────────────────────
    // STATS
    // ─────────────────────────────────────────────────────────────

    long countTotal(Business business);

    long countActive(Business business);

    long countLowStock(Business business);
}