package com.sbmp.inventory.purchase.service;

import com.sbmp.business.entity.Business;
import com.sbmp.business.repository.BusinessRepository;
import com.sbmp.inventory.category.entity.Category;
import com.sbmp.inventory.category.repository.CategoryRepository;
import com.sbmp.inventory.product.entity.Product;
import com.sbmp.inventory.product.repository.ProductRepository;
import com.sbmp.inventory.purchase.dto.*;
import com.sbmp.inventory.purchase.entity.Purchase;
import com.sbmp.inventory.purchase.entity.Purchase.PurchaseStatus;
import com.sbmp.inventory.purchase.entity.PurchaseItem;
import com.sbmp.inventory.purchase.entity.PurchasePayment;
import com.sbmp.inventory.purchase.repository.PurchaseRepository;
import com.sbmp.inventory.stock.service.StockService;
import com.sbmp.inventory.supplier.entity.Supplier;
import com.sbmp.inventory.supplier.repository.SupplierRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final BusinessRepository businessRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository  productRepository;
    private final CategoryRepository categoryRepository;
    private final StockService       stockService;       // ← Stock integration

    // ─────────────────────────────────────────────
    // 1. CREATE PURCHASE
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseResponseDTO createPurchase(PurchaseRequestDTO requestDTO) {
        log.info("Creating purchase invoice for supplier ID: {}", requestDTO.getSupplierId());

        Business business = businessRepository.findById(requestDTO.getBusinessId())
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + requestDTO.getBusinessId()));

        Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + requestDTO.getSupplierId()));

        String invoiceNumber = "PURCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Purchase purchase = Purchase.builder()
                .purchaseNumber(invoiceNumber)
                .supplier(supplier)
                .business(business)
                .purchaseDate(requestDTO.getPurchaseDate() != null ? requestDTO.getPurchaseDate() : LocalDate.now())
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : PurchaseStatus.DRAFT)
                .notes(requestDTO.getNotes())
                .subtotal(BigDecimal.ZERO)
                .totalDiscount(BigDecimal.ZERO)
                .grandTotal(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .dueAmount(BigDecimal.ZERO)
                .advancePaid(
                        requestDTO.getAdvancePaid() != null
                                ? requestDTO.getAdvancePaid()
                                : BigDecimal.ZERO
                )
                .items(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        processItemsAndPayments(purchase, requestDTO);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        // ── STOCK IN (শুধু COMPLETED হলে) ──────────
        if (savedPurchase.getStatus() == PurchaseStatus.COMPLETED) {
            updateStockForPurchase(savedPurchase);
        }

        return mapToResponseDTO(savedPurchase);
    }

    // ─────────────────────────────────────────────
    // 2. UPDATE PURCHASE
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseResponseDTO updatePurchase(Long id, PurchaseRequestDTO requestDTO) {
        log.info("Updating purchase invoice ID: {}", id);

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase record not found with ID: " + id));

        // পুরানো COMPLETED purchase এর stock reverse করা
        if (purchase.getStatus() == PurchaseStatus.COMPLETED) {
            reverseStockForPurchase(purchase);
        }

        Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + requestDTO.getSupplierId()));

        purchase.setSupplier(supplier);
        purchase.setPurchaseDate(requestDTO.getPurchaseDate());
        purchase.setStatus(requestDTO.getStatus());
        purchase.setNotes(requestDTO.getNotes());
        purchase.setAdvancePaid(
                requestDTO.getAdvancePaid() != null
                        ? requestDTO.getAdvancePaid()
                        : BigDecimal.ZERO
        );

        // চাইল্ড ডেটা ক্যাসকেড এতিম অপসারণ
        purchase.getItems().clear();
        purchase.getPayments().clear();

        processItemsAndPayments(purchase, requestDTO);

        Purchase updatedPurchase = purchaseRepository.save(purchase);

        // ── নতুন COMPLETED status হলে stock আবার দেওয়া ──
        if (updatedPurchase.getStatus() == PurchaseStatus.COMPLETED) {
            updateStockForPurchase(updatedPurchase);
        }

        return mapToResponseDTO(updatedPurchase);
    }

    // ─────────────────────────────────────────────
    // 3. GET PURCHASE BY ID
    // ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public PurchaseResponseDTO getPurchaseById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with ID: " + id));
        return mapToResponseDTO(purchase);
    }

    // ─────────────────────────────────────────────
    // 4. GET PURCHASE BY NUMBER
    // ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public PurchaseResponseDTO getPurchaseByNumber(String purchaseNumber) {
        Purchase purchase = purchaseRepository.findByPurchaseNumber(purchaseNumber)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with Number: " + purchaseNumber));
        return mapToResponseDTO(purchase);
    }

    // ─────────────────────────────────────────────
    // 5. SEARCH PURCHASES
    // ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseSummaryDTO> searchPurchases(Long businessId, PurchaseStatus status,
                                                     Long supplierId, LocalDate from, LocalDate to,
                                                     Pageable pageable) {
        log.info("Searching purchase index with filters for business ID: {}", businessId);
        return purchaseRepository.searchPurchases(businessId, status, supplierId, from, to, pageable)
                .map(this::mapToSummaryDTO);
    }

    // ─────────────────────────────────────────────
    // 6. COMPLETE PURCHASE (Draft → Completed)
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseResponseDTO completePurchase(Long id) {
        log.info("Finalizing purchase draft ID: {}", id);

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase draft not found with ID: " + id));

        if (purchase.getStatus() == PurchaseStatus.COMPLETED) {
            throw new IllegalStateException("This invoice is already completed.");
        }

        purchase.setStatus(PurchaseStatus.COMPLETED);
        Purchase saved = purchaseRepository.save(purchase);

        // ── STOCK IN ────────────────────────────────
        updateStockForPurchase(saved);

        return mapToResponseDTO(saved);
    }

    // ─────────────────────────────────────────────
    // 7. SAVE DRAFT
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseResponseDTO saveDraft(PurchaseRequestDTO requestDTO) {
        requestDTO.setStatus(PurchaseStatus.DRAFT);
        return createPurchase(requestDTO);
    }

    // ─────────────────────────────────────────────
    // 8. CANCEL PURCHASE
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public void cancelPurchase(Long id) {
        log.warn("Canceling purchase invoice ID: {}", id);

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase record not found with ID: " + id));

        // COMPLETED ছিল → stock reverse করা
        if (purchase.getStatus() == PurchaseStatus.COMPLETED) {
            reverseStockForPurchase(purchase);
        }

        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchaseRepository.save(purchase);
    }

    // ─────────────────────────────────────────────
    // 9. DELETE PURCHASE
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public void deletePurchase(Long id) {
        log.warn("Deleting purchase record ID: {}", id);

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase invoice not found. ID: " + id));

        if (purchase.getStatus() == PurchaseStatus.COMPLETED) {
            throw new IllegalStateException("Cannot delete a completed invoice. Cancel it first to adjust stocks safely.");
        }

        purchaseRepository.delete(purchase);
    }

    // ─────────────────────────────────────────────
    // 10. GET ALL BY BUSINESS
    // ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseSummaryDTO> getAllByBusiness(Long businessId) {
        return purchaseRepository
                .findByBusiness_IdOrderByCreatedAtDesc(businessId)
                .stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // STOCK HELPERS (private)
    // ─────────────────────────────────────────────

    /**
     * Purchase COMPLETED হলে প্রতিটা item এর জন্য stock in করা
     */
    private void updateStockForPurchase(Purchase purchase) {
        for (PurchaseItem item : purchase.getItems()) {
            try {
                stockService.stockIn(
                        item.getProduct().getId(),
                        purchase.getBusiness().getId(),
                        item.getPurchaseQty().intValue(),
                        item.getUnitPrice(),
                        "PURCHASE",
                        purchase.getPurchaseNumber(),
                        purchase.getId()
                );
            } catch (Exception e) {
                log.error("Stock update failed for product {}: {}", item.getProduct().getId(), e.getMessage());
            }
        }
    }

    /**
     * Purchase Cancel/Edit হলে আগের stock reverse করা
     */
    private void reverseStockForPurchase(Purchase purchase) {
        for (PurchaseItem item : purchase.getItems()) {
            try {
                stockService.stockOut(
                        item.getProduct().getId(),
                        purchase.getBusiness().getId(),
                        item.getPurchaseQty().intValue(),
                        "PURCHASE",
                        purchase.getPurchaseNumber(),
                        purchase.getId(),
                        "Reversed: Purchase " + (purchase.getStatus() == PurchaseStatus.CANCELLED
                                ? "cancelled" : "edited")
                );
            } catch (Exception e) {
                log.error("Stock reverse failed for product {}: {}", item.getProduct().getId(), e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────
    // INTERNAL CORE PROCESSOR (Items & Payments)
    // ─────────────────────────────────────────────
    private void processItemsAndPayments(Purchase purchase, PurchaseRequestDTO requestDTO) {
        BigDecimal subtotalAccumulator      = BigDecimal.ZERO;
        BigDecimal totalDiscountAccumulator = BigDecimal.ZERO;

        if (requestDTO.getItems() != null) {
            for (PurchaseItemRequestDTO itemDTO : requestDTO.getItems()) {
                if (itemDTO.getProductId() == null) continue;

                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found ID: " + itemDTO.getProductId()));

                Category category = product.getCategory();
                if (category == null && itemDTO.getCategoryId() != null) {
                    category = categoryRepository.findById(itemDTO.getCategoryId()).orElse(null);
                }

                BigDecimal currentStockSnapshot  = BigDecimal.valueOf(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
                BigDecimal previousPriceSnapshot = product.getPurchasePrice() != null ? product.getPurchasePrice() : BigDecimal.ZERO;

                PurchaseItem item = PurchaseItem.builder()
                        .product(product)
                        .category(category)
                        .oldStock(currentStockSnapshot)
                        .lastPurchasePrice(previousPriceSnapshot)
                        .purchaseQty(itemDTO.getPurchaseQty())
                        .unitPrice(itemDTO.getUnitPrice())
                        .discountAmount(itemDTO.getDiscountAmount() != null ? itemDTO.getDiscountAmount() : BigDecimal.ZERO)
                        .discountPercentage(itemDTO.getDiscountPercentage() != null ? itemDTO.getDiscountPercentage() : BigDecimal.ZERO)
                        .build();

                item.setItemTotal(item.calculateItemTotal());

                BigDecimal rawLineSubtotal = itemDTO.getPurchaseQty().multiply(itemDTO.getUnitPrice());
                subtotalAccumulator      = subtotalAccumulator.add(rawLineSubtotal);
                totalDiscountAccumulator = totalDiscountAccumulator.add(item.getEffectiveDiscount());

                purchase.addItem(item);

                // NOTE: Stock update এখন updateStockForPurchase() থেকে হয়।
                // processItemsAndPayments এ stock update সরানো হয়েছে
                // যাতে duplicate update না হয়।
                // Product price update শুধু এখানেই করা হচ্ছে।
                if (purchase.getStatus() == PurchaseStatus.COMPLETED) {
                    product.setLastPurchasePrice(previousPriceSnapshot);
                    product.setPurchasePrice(itemDTO.getUnitPrice());
                    productRepository.save(product);
                }
            }
        }

        purchase.setSubtotal(subtotalAccumulator);
        purchase.setTotalDiscount(totalDiscountAccumulator);
        BigDecimal grandTotal = subtotalAccumulator.subtract(totalDiscountAccumulator).max(BigDecimal.ZERO);
        purchase.setGrandTotal(grandTotal);

        BigDecimal totalPaidAccumulator = BigDecimal.ZERO;
        if (requestDTO.getPayments() != null) {
            for (PurchasePaymentRequestDTO paymentDTO : requestDTO.getPayments()) {
                if (paymentDTO.getAmount() == null || paymentDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) continue;

                PurchasePayment payment = PurchasePayment.builder()
                        .paymentMethod(paymentDTO.getPaymentMethod())
                        .amount(paymentDTO.getAmount())
                        .advanceAmount(paymentDTO.getAdvanceAmount() != null ? paymentDTO.getAdvanceAmount() : BigDecimal.ZERO)
                        .referenceNo(paymentDTO.getReferenceNo())
                        .accountInfo(paymentDTO.getAccountInfo())
                        .paymentDate(paymentDTO.getPaymentDate() != null ? paymentDTO.getPaymentDate() : LocalDate.now())
                        .notes(paymentDTO.getNotes())
                        .build();

                purchase.addPayment(payment);
                totalPaidAccumulator = totalPaidAccumulator.add(paymentDTO.getAmount());
            }
        }

        BigDecimal advancePaid =
                purchase.getAdvancePaid() != null
                        ? purchase.getAdvancePaid()
                        : BigDecimal.ZERO;

        BigDecimal totalPaid =
                totalPaidAccumulator.add(advancePaid);

        purchase.setPaidAmount(totalPaid);

        purchase.setDueAmount(
                grandTotal.subtract(totalPaid)
                        .max(BigDecimal.ZERO)
        );
    }

    // ─────────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────────
    private PurchaseResponseDTO mapToResponseDTO(Purchase p) {
        return PurchaseResponseDTO.builder()
                .id(p.getId())
                .purchaseNumber(p.getPurchaseNumber())
                .supplierId(p.getSupplier().getId())
                .supplierName(p.getSupplier().getSupplierName())
                .supplierPhone(p.getSupplier().getPhone())
                .businessId(p.getBusiness().getId())
                .businessName(p.getBusiness().getBusinessName())
                .purchaseDate(p.getPurchaseDate())
                .status(p.getStatus())
                .subtotal(p.getSubtotal())
                .totalDiscount(p.getTotalDiscount())
                .grandTotal(p.getGrandTotal())
                .paidAmount(p.getPaidAmount())
                .advancePaid(p.getAdvancePaid())
                .dueAmount(p.getDueAmount())
                .notes(p.getNotes())
                .items(p.getItems().stream().map(this::mapItemToResponseDTO).collect(Collectors.toList()))
                .payments(p.getPayments().stream().map(this::mapPaymentToResponseDTO).collect(Collectors.toList()))
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private PurchaseItemResponseDTO mapItemToResponseDTO(PurchaseItem item) {
        return PurchaseItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .oldStock(item.getOldStock())
                .lastPurchasePrice(item.getLastPurchasePrice())
                .purchaseQty(item.getPurchaseQty())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .discountPercentage(item.getDiscountPercentage())
                .itemTotal(item.getItemTotal())
                .build();
    }

    private PurchasePaymentResponseDTO mapPaymentToResponseDTO(PurchasePayment payment) {
        return PurchasePaymentResponseDTO.builder()
                .id(payment.getId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .advanceAmount(payment.getAdvanceAmount())
                .referenceNo(payment.getReferenceNo())
                .accountInfo(payment.getAccountInfo())
                .paymentDate(payment.getPaymentDate())
                .notes(payment.getNotes())
                .build();
    }

    private PurchaseSummaryDTO mapToSummaryDTO(Purchase p) {
        return PurchaseSummaryDTO.builder()
                .id(p.getId())
                .purchaseNumber(p.getPurchaseNumber())
                .supplierName(p.getSupplier().getSupplierName())
                .purchaseDate(p.getPurchaseDate())
                .status(p.getStatus())
                .grandTotal(p.getGrandTotal())
                .paidAmount(p.getPaidAmount())
                .dueAmount(p.getDueAmount())
                .advancePaid(p.getAdvancePaid())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
