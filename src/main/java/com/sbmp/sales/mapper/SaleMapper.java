package com.sbmp.sales.mapper;

import com.sbmp.sales.dto.SaleItemResponseDto;
import com.sbmp.sales.dto.SaleResponseDto;
import com.sbmp.sales.entity.Sale;
import com.sbmp.sales.entity.SaleItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SaleMapper {

    public SaleResponseDto toResponseDto(
            Sale sale
    ) {

        return SaleResponseDto.builder()
                .id(sale.getId())
                .invoiceNo(sale.getInvoiceNo())

                .customerId(
                        sale.getCustomer().getId()
                )

                .customerName(
                        sale.getCustomer().getName()
                )

                .businessId(
                        sale.getBusiness().getId()
                )

                .businessName(
                        sale.getBusiness().getBusinessName()
                )

                .saleDate(
                        sale.getSaleDate()
                )

                .status(
                        sale.getStatus()
                )

                .paymentStatus(
                        sale.getPaymentStatus()
                )

                .subtotal(
                        sale.getSubtotal()
                )

                .totalDiscount(
                        sale.getTotalDiscount()
                )

                .grandTotal(
                        sale.getGrandTotal()
                )

                .paidAmount(
                        sale.getPaidAmount()
                )

                .dueAmount(
                        sale.getDueAmount()
                )

                .advancePaid(
                        sale.getAdvancePaid()
                )

                .notes(
                        sale.getNotes()
                )

                .items(
                        sale.getItems()
                                .stream()
                                .map(this::toItemResponseDto)
                                .collect(Collectors.toList())
                )

                .createdAt(
                        sale.getCreatedAt()
                )

                .updatedAt(
                        sale.getUpdatedAt()
                )

                .build();
    }

    public SaleItemResponseDto toItemResponseDto(
            SaleItem item
    ) {

        return SaleItemResponseDto.builder()

                .id(item.getId())

                .productId(
                        item.getProduct().getId()
                )

                .productName(
                        item.getProduct().getName()
                )

                .categoryName(
                        item.getCategory().getName()
                )

                .stockBefore(
                        item.getStockBefore()
                )

                .stockAfter(
                        item.getStockAfter()
                )

                .quantity(
                        item.getQuantity()
                )

                .unitPrice(
                        item.getUnitPrice()
                )

                .discountAmount(
                        item.getDiscountAmount()
                )

                .discountPercentage(
                        item.getDiscountPercentage()
                )

                .itemTotal(
                        item.getItemTotal()
                )

                .build();
    }
}