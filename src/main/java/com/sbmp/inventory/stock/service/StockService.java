package com.sbmp.inventory.stock.service;

import com.sbmp.inventory.stock.entity.Stock;
import com.sbmp.inventory.stock.entity.StockTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface StockService {

    // ─────────────────────────────────────────────
    // STOCK IN
    // Purchase
    // Sale Cancel
    // Sale Return
    // ─────────────────────────────────────────────

    void stockIn(
            Long productId,
            Long businessId,
            int quantity,
            BigDecimal unitPrice,
            String referenceType,
            String referenceNumber,
            Long referenceId
    );

    // ─────────────────────────────────────────────
    // STOCK OUT
    // Sale
    // Purchase Cancel
    // ─────────────────────────────────────────────

    void stockOut(
            Long productId,
            Long businessId,
            int quantity,
            String referenceType,
            String referenceNumber,
            Long referenceId,
            String notes
    );

    // ─────────────────────────────────────────────
    // MANUAL ADJUSTMENT
    // ─────────────────────────────────────────────

    void adjustStock(
            Long productId,
            Long businessId,
            int newQuantity,
            String notes
    );

    // ─────────────────────────────────────────────
    // REVERSE TRANSACTIONS
    // Purchase Cancel
    // Purchase Edit
    // ─────────────────────────────────────────────

    void reverseTransactions(
            Long referenceId,
            String referenceType,
            Long businessId
    );

    // ─────────────────────────────────────────────
    // STOCK LOOKUP
    // ─────────────────────────────────────────────

    Stock getStock(
            Long productId,
            Long businessId
    );

    List<Stock> getAllStocks(
            Long businessId
    );

    List<Stock> getLowStocks(
            Long businessId
    );

    List<Stock> getOutOfStocks(
            Long businessId
    );

    // ─────────────────────────────────────────────
    // HISTORY
    // ─────────────────────────────────────────────

    List<StockTransaction> getProductHistory(
            Long productId,
            Long businessId
    );

    // ─────────────────────────────────────────────
    // DASHBOARD
    // ─────────────────────────────────────────────

    BigDecimal getInventoryValue(
            Long businessId
    );
}