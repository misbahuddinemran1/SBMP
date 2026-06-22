package com.sbmp.inventory.product.mapper;

import com.sbmp.inventory.product.dto.request.CreateProductRequest;
import com.sbmp.inventory.product.dto.request.UpdateProductRequest;
import com.sbmp.inventory.product.dto.response.ProductResponse;
import com.sbmp.inventory.product.entity.Product;
import org.springframework.stereotype.Component;

/**
 * eLoan SaaS — ProductMapper
 */
@Component
public class ProductMapper {

    // ─────────────────────────────────────────────────────────────
    // CREATE DTO → ENTITY
    // ─────────────────────────────────────────────────────────────

    public Product toEntity(
            CreateProductRequest request
    ) {

        return Product.builder()

                .name(request.getName())

                .sku(request.getSku())

                .barcode(request.getBarcode())

                .description(request.getDescription())

                .purchasePrice(
                        request.getPurchasePrice()
                )

                .sellingPrice(
                        request.getSellingPrice()
                )

                .stockQuantity(
                        request.getStockQuantity()
                )

                .minimumStockAlert(
                        request.getMinimumStockAlert()
                )

                .unit(request.getUnit())

                .active(request.getActive())

                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE DTO → ENTITY
    // ─────────────────────────────────────────────────────────────

    public void updateEntity(
            UpdateProductRequest request,
            Product product
    ) {

        product.setName(request.getName());

        product.setSku(request.getSku());

        product.setBarcode(request.getBarcode());

        product.setDescription(
                request.getDescription()
        );

        product.setPurchasePrice(
                request.getPurchasePrice()
        );

        product.setSellingPrice(
                request.getSellingPrice()
        );

        product.setStockQuantity(
                request.getStockQuantity()
        );

        product.setMinimumStockAlert(
                request.getMinimumStockAlert()
        );

        product.setUnit(request.getUnit());

        product.setActive(
                request.getActive()
        );
    }

    // ─────────────────────────────────────────────────────────────
    // ENTITY → RESPONSE DTO
    // ─────────────────────────────────────────────────────────────

    public ProductResponse toResponse(
            Product product
    ) {

        return ProductResponse.builder()

                .id(product.getId())

                .name(product.getName())

                .sku(product.getSku())

                .barcode(product.getBarcode())

                .description(
                        product.getDescription()
                )

                .purchasePrice(
                        product.getPurchasePrice()
                )

                .sellingPrice(
                        product.getSellingPrice()
                )

                .stockQuantity(
                        product.getStockQuantity()
                )

                .minimumStockAlert(
                        product.getMinimumStockAlert()
                )

                .unit(product.getUnit())

                .active(product.getActive())

                .lowStock(
                        product.isLowStock()
                )

                .outOfStock(
                        product.isOutOfStock()
                )

                .categoryId(
                        product.getCategory().getId()
                )

                .categoryName(
                        product.getCategory().getName()
                )

                .businessId(
                        product.getBusiness().getId()
                )

                .businessName(
                        product.getBusiness()
                                .getBusinessName()
                )

                .createdAt(
                        product.getCreatedAt()
                )

                .updatedAt(
                        product.getUpdatedAt()
                )

                .build();
    }
}