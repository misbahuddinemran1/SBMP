package com.sbmp.sales.service.impl;

import com.sbmp.business.entity.Business;
import com.sbmp.business.repository.BusinessRepository;
import com.sbmp.customer.entity.Customer;
import com.sbmp.customer.repository.CustomerRepository;
import com.sbmp.inventory.product.entity.Product;
import com.sbmp.inventory.product.repository.ProductRepository;
import com.sbmp.sales.dto.SaleItemRequestDto;
import com.sbmp.sales.dto.SaleItemResponseDto;
import com.sbmp.sales.dto.SaleRequestDto;
import com.sbmp.sales.dto.SaleResponseDto;
import com.sbmp.sales.entity.Sale;
import com.sbmp.sales.entity.SaleItem;
import com.sbmp.sales.enums.PaymentStatus;
import com.sbmp.sales.enums.SaleStatus;
import com.sbmp.sales.repository.SaleRepository;
import com.sbmp.sales.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository      saleRepository;
    private final CustomerRepository  customerRepository;
    private final BusinessRepository  businessRepository;
    private final ProductRepository   productRepository;

    // ─────────────────────────────────────────────
    // CREATE SALE
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public SaleResponseDto createSale(SaleRequestDto dto) {

        // ── 1. Load Customer ─────────────────────
        Customer customer = customerRepository
                .findById(dto.getCustomerId())
                .orElseThrow(() ->
                        new RuntimeException("Customer not found: "
                                + dto.getCustomerId()));

        // ── 2. Load Business ─────────────────────
        Business business = businessRepository
                .findById(dto.getBusinessId())
                .orElseThrow(() ->
                        new RuntimeException("Business not found: "
                                + dto.getBusinessId()));

        // ── 3. Generate Invoice Number ───────────
        String invoiceNo = generateInvoiceNo();

        // ── 4. Build Sale entity ─────────────────
        Sale sale = Sale.builder()
                .invoiceNo(invoiceNo)
                .customer(customer)
                .business(business)
                .saleDate(dto.getSaleDate() != null
                        ? dto.getSaleDate()
                        : LocalDate.now())
                .notes(dto.getNotes())
                .advancePaid(dto.getAdvancePaid() != null
                        ? dto.getAdvancePaid()
                        : BigDecimal.ZERO)
                .status(dto.getStatus() != null
                        ? dto.getStatus()
                        : SaleStatus.DRAFT)
                .subtotal(BigDecimal.ZERO)
                .totalDiscount(BigDecimal.ZERO)
                .grandTotal(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .dueAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.DUE)
                .build();

        // ── 5. Process Items ─────────────────────
        BigDecimal subtotal      = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (SaleItemRequestDto itemDto : dto.getItems()) {

            // Load product
            Product product = productRepository
                    .findById(itemDto.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException("Product not found: "
                                    + itemDto.getProductId()));

            // Stock validation (only for COMPLETED)
            if (SaleStatus.COMPLETED.equals(dto.getStatus())) {
                if (product.getStockQuantity() < itemDto.getQuantity()) {
                    throw new RuntimeException(
                            "Insufficient stock for: " + product.getName()
                                    + ". Available: " + product.getStockQuantity()
                                    + ", Requested: " + itemDto.getQuantity()
                    );
                }
            }

            // Resolve discount amount
            BigDecimal discountAmount = resolveDiscountAmount(
                    itemDto,
                    itemDto.getQuantity(),
                    itemDto.getUnitPrice()
            );

            BigDecimal discountPct = resolveDiscountPercentage(
                    itemDto,
                    itemDto.getQuantity(),
                    itemDto.getUnitPrice()
            );

            // Line total = (qty × unitPrice) - discountAmount
            BigDecimal lineSubtotal = itemDto.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            BigDecimal itemTotal = lineSubtotal
                    .subtract(discountAmount)
                    .max(BigDecimal.ZERO);

            // Stock snapshot
            int stockBefore = product.getStockQuantity();
            int stockAfter  = stockBefore; // unchanged for DRAFT

            // Deduct stock only if COMPLETED
            if (SaleStatus.COMPLETED.equals(dto.getStatus())) {
                stockAfter = stockBefore - itemDto.getQuantity();
                product.setStockQuantity(stockAfter);
                productRepository.save(product);
            }

            // Build SaleItem
            SaleItem saleItem = SaleItem.builder()
                    .product(product)
                    .category(product.getCategory())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .discountAmount(discountAmount)
                    .discountPercentage(discountPct)
                    .itemTotal(itemTotal)
                    .stockBefore(stockBefore)
                    .stockAfter(stockAfter)
                    .build();

            sale.addItem(saleItem);

            subtotal      = subtotal.add(lineSubtotal);
            totalDiscount = totalDiscount.add(discountAmount);
        }

        // ── 6. Calculate Totals ──────────────────
        BigDecimal grandTotal  = subtotal.subtract(totalDiscount)
                .max(BigDecimal.ZERO);

        BigDecimal advancePaid = sale.getAdvancePaid();
        BigDecimal dueAmount   = grandTotal.subtract(advancePaid)
                .max(BigDecimal.ZERO);

        // Payment status
        PaymentStatus paymentStatus;
        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {
            paymentStatus = PaymentStatus.PAID;
        } else if (advancePaid.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = PaymentStatus.PARTIAL;
        } else {
            paymentStatus = PaymentStatus.DUE;
        }

        sale.setSubtotal(subtotal);
        sale.setTotalDiscount(totalDiscount);
        sale.setGrandTotal(grandTotal);
        sale.setPaidAmount(advancePaid);
        sale.setDueAmount(dueAmount);
        sale.setPaymentStatus(paymentStatus);

        // ── 7. Save ──────────────────────────────
        Sale saved = saleRepository.save(sale);

        // ── 8. Return Response ───────────────────
        return mapToResponse(saved);
    }

    // ─────────────────────────────────────────────
    // INVOICE NUMBER GENERATOR
    // INV-20240604-001 format
    // ─────────────────────────────────────────────
    private String generateInvoiceNo() {

        String datePart = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String prefix = "INV-" + datePart + "-";

        // Count existing invoices with same date prefix
        long count = saleRepository
                .countByInvoiceNoStartingWith(prefix);

        String sequence = String.format("%03d", count + 1);

        String invoiceNo = prefix + sequence;

        // Collision guard
        while (saleRepository.existsByInvoiceNo(invoiceNo)) {
            count++;
            sequence  = String.format("%03d", count + 1);
            invoiceNo = prefix + sequence;
        }

        return invoiceNo;
    }

    // ─────────────────────────────────────────────
    // DISCOUNT HELPERS
    // ─────────────────────────────────────────────
    private BigDecimal resolveDiscountAmount(
            SaleItemRequestDto dto,
            int qty,
            BigDecimal unitPrice
    ) {
        // If discountAmount given directly — use it
        if (dto.getDiscountAmount() != null
                && dto.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            return dto.getDiscountAmount();
        }

        // Else calculate from percentage
        if (dto.getDiscountPercentage() != null
                && dto.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(qty));
            return lineTotal
                    .multiply(dto.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal resolveDiscountPercentage(
            SaleItemRequestDto dto,
            int qty,
            BigDecimal unitPrice
    ) {
        if (dto.getDiscountPercentage() != null
                && dto.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            return dto.getDiscountPercentage();
        }

        if (dto.getDiscountAmount() != null
                && dto.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(qty));
            if (lineTotal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
            return dto.getDiscountAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(lineTotal, 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    // ─────────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────────
    private SaleResponseDto mapToResponse(Sale sale) {

        List<SaleItemResponseDto> itemDtos = sale.getItems()
                .stream()
                .map(item -> SaleItemResponseDto.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .categoryName(item.getCategory().getName())
                        .stockBefore(item.getStockBefore())
                        .stockAfter(item.getStockAfter())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discountAmount(item.getDiscountAmount())
                        .discountPercentage(item.getDiscountPercentage())
                        .itemTotal(item.getItemTotal())
                        .build())
                .collect(Collectors.toList());

        return SaleResponseDto.builder()
                .id(sale.getId())
                .invoiceNo(sale.getInvoiceNo())
                .customerId(sale.getCustomer().getId())
                .customerName(sale.getCustomer().getName())
                .businessId(sale.getBusiness().getId())
                .businessName(sale.getBusiness().getBusinessName())
                .saleDate(sale.getSaleDate())
                .status(sale.getStatus())
                .paymentStatus(sale.getPaymentStatus())
                .subtotal(sale.getSubtotal())
                .totalDiscount(sale.getTotalDiscount())
                .grandTotal(sale.getGrandTotal())
                .paidAmount(sale.getPaidAmount())
                .dueAmount(sale.getDueAmount())
                .advancePaid(sale.getAdvancePaid())
                .notes(sale.getNotes())
                .items(itemDtos)
                .createdAt(sale.getCreatedAt())
                .updatedAt(sale.getUpdatedAt())
                .build();
    }
}