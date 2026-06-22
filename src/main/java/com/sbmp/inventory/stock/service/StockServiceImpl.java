package com.sbmp.inventory.stock.service;

import com.sbmp.business.entity.Business;
import com.sbmp.business.repository.BusinessRepository;
import com.sbmp.inventory.product.entity.Product;
import com.sbmp.inventory.product.repository.ProductRepository;
import com.sbmp.inventory.stock.entity.Stock;
import com.sbmp.inventory.stock.entity.StockTransaction;
import com.sbmp.inventory.stock.repository.StockRepository;
import com.sbmp.inventory.stock.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;

    // =====================================================
    // STOCK IN
    // =====================================================

    @Override
    public void stockIn(
            Long productId,
            Long businessId,
            int quantity,
            BigDecimal unitPrice,
            String referenceType,
            String referenceNumber,
            Long referenceId
    ) {

        Product product = getProduct(productId);
        Business business = getBusiness(businessId);

        Stock stock = stockRepository
                .findByProduct_IdAndBusiness_Id(
                        productId,
                        businessId
                )
                .orElseGet(() -> Stock.builder()
                        .product(product)
                        .business(business)
                        .currentQuantity(0)
                        .minimumStockAlert(
                                product.getMinimumStockAlert() != null
                                        ? product.getMinimumStockAlert()
                                        : 5
                        )
                        .unit(product.getUnit())
                        .build());

        int before = stock.getCurrentQuantity();
        int after = before + quantity;

        updateAverageCost(
                stock,
                before,
                quantity,
                unitPrice
        );

        stock.setCurrentQuantity(after);
        product.setStockQuantity(after);
        stock.setLastPurchasePrice(unitPrice);

        stockRepository.save(stock);

        // Sync Product Table
        product.setStockQuantity(after);
        product.setLastPurchasePrice(unitPrice);

        productRepository.save(product);

        saveTransaction(
                product,
                business,
                StockTransaction.TransactionType.PURCHASE_IN,
                before,
                quantity,
                after,
                referenceType,
                referenceNumber,
                referenceId,
                unitPrice,
                stock.getAverageCost(),
                "Stock In"
        );
    }

    // =====================================================
    // STOCK OUT
    // =====================================================

    @Override
    public void stockOut(
            Long productId,
            Long businessId,
            int quantity,
            String referenceType,
            String referenceNumber,
            Long referenceId,
            String notes
    ) {

        Product product = getProduct(productId);
        Business business = getBusiness(businessId);

        Stock stock = stockRepository
                .findByProduct_IdAndBusiness_Id(
                        productId,
                        businessId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Stock not found"
                        ));

        int before = stock.getCurrentQuantity();

        if (before < quantity) {

            throw new RuntimeException(
                    "Insufficient stock for product: "
                            + product.getName()
            );
        }

        int after = before - quantity;

        stock.setCurrentQuantity(after);

        stockRepository.save(stock);

        product.setStockQuantity(after);

        productRepository.save(product);

        StockTransaction.TransactionType type =
                "PURCHASE".equalsIgnoreCase(referenceType)
                        ? StockTransaction.TransactionType.PURCHASE_CANCEL
                        : StockTransaction.TransactionType.SALE_OUT;

        saveTransaction(
                product,
                business,
                type,
                before,
                -quantity,
                after,
                referenceType,
                referenceNumber,
                referenceId,
                null,
                stock.getAverageCost(),
                notes
        );
    }

    // =====================================================
    // ADJUSTMENT
    // =====================================================

    @Override
    public void adjustStock(
            Long productId,
            Long businessId,
            int newQuantity,
            String notes
    ) {

        Product product = getProduct(productId);
        Business business = getBusiness(businessId);

        Stock stock = stockRepository
                .findByProduct_IdAndBusiness_Id(
                        productId,
                        businessId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Stock not found"
                        ));

        int before = stock.getCurrentQuantity();

        int changed = newQuantity - before;

        stock.setCurrentQuantity(newQuantity);

        stockRepository.save(stock);

        product.setStockQuantity(newQuantity);

        productRepository.save(product);

        saveTransaction(
                product,
                business,
                StockTransaction.TransactionType.ADJUSTMENT,
                before,
                changed,
                newQuantity,
                "ADJUSTMENT",
                "ADJ",
                null,
                null,
                stock.getAverageCost(),
                notes
        );
    }

    // =====================================================
    // REVERSE
    // =====================================================

    @Override
    public void reverseTransactions(
            Long referenceId,
            String referenceType,
            Long businessId
    ) {

        List<StockTransaction> transactions =
                transactionRepository
                        .findByReferenceIdAndReferenceType(
                                referenceId,
                                referenceType
                        );

        for (StockTransaction txn : transactions) {

            if (txn.getQuantityChanged() > 0) {

                stockOut(
                        txn.getProduct().getId(),
                        businessId,
                        txn.getQuantityChanged(),
                        referenceType,
                        txn.getReferenceNumber(),
                        referenceId,
                        "Reverse Transaction"
                );

            } else {

                stockIn(
                        txn.getProduct().getId(),
                        businessId,
                        Math.abs(txn.getQuantityChanged()),
                        txn.getUnitPrice(),
                        referenceType,
                        txn.getReferenceNumber(),
                        referenceId
                );
            }
        }
    }

    // =====================================================
    // LOOKUPS
    // =====================================================

    @Override
    public Stock getStock(Long productId, Long businessId) {

        return stockRepository
                .findByProduct_IdAndBusiness_Id(
                        productId,
                        businessId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Stock not found"
                        ));
    }

    @Override
    public List<Stock> getAllStocks(Long businessId) {

        return stockRepository
                .findByBusiness_IdOrderByProduct_NameAsc(
                        businessId
                );
    }

    @Override
    public List<Stock> getLowStocks(Long businessId) {

        return stockRepository
                .findLowStockByBusiness(
                        businessId
                );
    }

    @Override
    public List<Stock> getOutOfStocks(Long businessId) {

        return stockRepository
                .findOutOfStockByBusiness(
                        businessId
                );
    }

    @Override
    public List<StockTransaction> getProductHistory(
            Long productId,
            Long businessId
    ) {

        return transactionRepository
                .findByProduct_IdAndBusiness_IdOrderByCreatedAtDesc(
                        productId,
                        businessId
                );
    }

    @Override
    public BigDecimal getInventoryValue(
            Long businessId
    ) {

        return stockRepository
                .getTotalStockValue(
                        businessId
                );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Product getProduct(Long id) {

        return productRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found: " + id
                        ));
    }

    private Business getBusiness(Long id) {

        return businessRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Business not found: " + id
                        ));
    }

    private void updateAverageCost(
            Stock stock,
            int beforeQty,
            int purchaseQty,
            BigDecimal purchasePrice
    ) {

        if (purchasePrice == null ||
                purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (beforeQty == 0) {

            stock.setAverageCost(
                    purchasePrice
            );

            return;
        }

        BigDecimal oldValue =
                stock.getAverageCost()
                        .multiply(
                                BigDecimal.valueOf(beforeQty)
                        );

        BigDecimal newValue =
                purchasePrice.multiply(
                        BigDecimal.valueOf(purchaseQty)
                );

        BigDecimal average =
                oldValue.add(newValue)
                        .divide(
                                BigDecimal.valueOf(
                                        beforeQty + purchaseQty
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );

        stock.setAverageCost(average);
    }

    private void saveTransaction(
            Product product,
            Business business,
            StockTransaction.TransactionType type,
            int before,
            int changed,
            int after,
            String refType,
            String refNumber,
            Long refId,
            BigDecimal unitPrice,
            BigDecimal averageCost,
            String notes
    ) {

        transactionRepository.save(

                StockTransaction.builder()
                        .product(product)
                        .business(business)
                        .transactionType(type)
                        .quantityBefore(before)
                        .quantityChanged(changed)
                        .quantityAfter(after)
                        .referenceType(refType)
                        .referenceNumber(refNumber)
                        .referenceId(refId)
                        .unitPrice(unitPrice)
                        .averageCost(averageCost)
                        .notes(notes)
                        .build()
        );
    }
}