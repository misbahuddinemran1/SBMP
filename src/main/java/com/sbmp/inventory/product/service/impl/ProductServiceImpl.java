package com.sbmp.inventory.product.service.impl;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.product.entity.Product;
import com.sbmp.inventory.product.repository.ProductRepository;
import com.sbmp.inventory.product.service.ProductService;
import com.sbmp.inventory.stock.entity.Stock;
import com.sbmp.inventory.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * eLoan SaaS — ProductServiceImpl
 * Product business logic implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl
        implements ProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    // ─────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Product save(Product product) {

        // SKU VALIDATION

        if (product.getSku() != null
                && !product.getSku().isBlank()
                && productRepository
                .existsBySkuIgnoreCaseAndBusiness(
                        product.getSku(),
                        product.getBusiness()
                )) {

            throw new RuntimeException(
                    "SKU already exists."
            );
        }

        // BARCODE VALIDATION

        if (product.getBarcode() != null
                && !product.getBarcode().isBlank()
                && productRepository
                .existsByBarcodeAndBusiness(
                        product.getBarcode(),
                        product.getBusiness()
                )) {

            throw new RuntimeException(
                    "Barcode already exists."
            );
        }

        Product saved =
                productRepository.save(product);

        Stock stock = Stock.builder()
                .product(saved)
                .business(saved.getBusiness())
                .currentQuantity(
                        saved.getStockQuantity()
                )
                .minimumStockAlert(
                        saved.getMinimumStockAlert()
                )
                .unit(
                        saved.getUnit()
                )
                .averageCost(
                        saved.getPurchasePrice()
                )
                .lastPurchasePrice(
                        saved.getPurchasePrice()
                )
                .build();

        stockRepository.save(stock);

        log.info(
                "Product created: id={}, name={}",
                saved.getId(),
                saved.getName()
        );

        return saved;
    }

    @Override
    @Transactional
    public Product update(Long id,
                          Product updated) {

        Product existing =
                getByIdAndBusiness(
                        id,
                        updated.getBusiness()
                );

        // SKU VALIDATION

        if (updated.getSku() != null
                && !updated.getSku().isBlank()
                && productRepository
                .existsBySkuIgnoreCaseAndIdNotAndBusiness(
                        updated.getSku(),
                        id,
                        updated.getBusiness()
                )) {

            throw new RuntimeException(
                    "SKU already exists."
            );
        }

        // BARCODE VALIDATION

        if (updated.getBarcode() != null
                && !updated.getBarcode().isBlank()
                && productRepository
                .existsByBarcodeAndIdNotAndBusiness(
                        updated.getBarcode(),
                        id,
                        updated.getBusiness()
                )) {

            throw new RuntimeException(
                    "Barcode already exists."
            );
        }

        existing.setName(updated.getName());

        existing.setSku(updated.getSku());

        existing.setBarcode(
                updated.getBarcode()
        );

        existing.setDescription(
                updated.getDescription()
        );

        existing.setPurchasePrice(
                updated.getPurchasePrice()
        );

        existing.setSellingPrice(
                updated.getSellingPrice()
        );

        existing.setStockQuantity(
                updated.getStockQuantity()
        );

        existing.setMinimumStockAlert(
                updated.getMinimumStockAlert()
        );

        existing.setUnit(
                updated.getUnit()
        );

        existing.setCategory(
                updated.getCategory()
        );

        existing.setActive(
                updated.getActive()
        );

        Product saved =
                productRepository.save(existing);
        Stock stock =
                stockRepository
                        .findByProduct_IdAndBusiness_Id(
                                existing.getId(),
                                existing.getBusiness().getId()
                        )
                        .orElse(null);

        if (stock != null) {

            stock.setCurrentQuantity(
                    existing.getStockQuantity()
            );

            stock.setMinimumStockAlert(
                    existing.getMinimumStockAlert()
            );

            stock.setUnit(
                    existing.getUnit()
            );

            stockRepository.save(stock);
        }

        log.info(
                "Product updated: id={}, name={}",
                saved.getId(),
                saved.getName()
        );

        return saved;
    }

    @Override
    @Transactional
    public void deleteByIdAndBusiness(
            Long id,
            Business business
    ) {

        Product product =
                getByIdAndBusiness(
                        id,
                        business
                );

        productRepository.delete(product);

        log.info(
                "Product deleted: id={}, name={}",
                id,
                product.getName()
        );
    }

    // ─────────────────────────────────────────────────────────────
    // FINDERS
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {

        return productRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found."
                        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Product getByIdAndBusiness(
            Long id,
            Business business
    ) {

        return productRepository
                .findByIdAndBusiness(
                        id,
                        business
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found."
                        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findBySku(
            String sku,
            Business business
    ) {

        return productRepository
                .findBySkuAndBusiness(
                        sku,
                        business
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findByBarcode(
            String barcode,
            Business business
    ) {

        return productRepository
                .findByBarcodeAndBusiness(
                        barcode,
                        business
                );
    }

    // ─────────────────────────────────────────────────────────────
    // LISTING
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAll(
            Business business,
            Pageable pageable
    ) {

        return productRepository
                .findByBusiness(
                        business,
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(
            Business business,
            String keyword,
            Pageable pageable
    ) {

        String kw =
                (keyword != null
                        && !keyword.isBlank())
                        ? keyword.trim()
                        : null;

        return productRepository.search(
                business,
                kw,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAllActive(
            Business business
    ) {

        return productRepository
                .findByBusinessAndActiveTrue(
                        business
                );
    }

    // ─────────────────────────────────────────────────────────────
    // LOW STOCK
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(
            Business business
    ) {

        return productRepository
                .findLowStockProducts(
                        business
                );
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDATION
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySku(
            Business business,
            String sku
    ) {

        if (sku == null
                || sku.isBlank()) {

            return false;
        }

        return productRepository
                .existsBySkuIgnoreCaseAndBusiness(
                        sku,
                        business
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByBarcode(
            Business business,
            String barcode
    ) {

        if (barcode == null
                || barcode.isBlank()) {

            return false;
        }

        return productRepository
                .existsByBarcodeAndBusiness(
                        barcode,
                        business
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySkuAndIdNot(
            Business business,
            String sku,
            Long id
    ) {

        if (sku == null
                || sku.isBlank()) {

            return false;
        }

        return productRepository
                .existsBySkuIgnoreCaseAndIdNotAndBusiness(
                        sku,
                        id,
                        business
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByBarcodeAndIdNot(
            Business business,
            String barcode,
            Long id
    ) {

        if (barcode == null
                || barcode.isBlank()) {

            return false;
        }

        return productRepository
                .existsByBarcodeAndIdNotAndBusiness(
                        barcode,
                        id,
                        business
                );
    }

    // ─────────────────────────────────────────────────────────────
    // STATS
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long countTotal(
            Business business
    ) {

        return productRepository
                .countByBusiness(
                        business
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive(
            Business business
    ) {

        return productRepository
                .countByBusinessAndActive(
                        business,
                        true
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long countLowStock(
            Business business
    ) {

        return productRepository
                .countByBusinessAndStockQuantityLessThanEqual(
                        business,
                        5
                );
    }
}