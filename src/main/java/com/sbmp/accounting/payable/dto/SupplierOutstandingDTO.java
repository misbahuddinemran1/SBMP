package com.sbmp.accounting.payable.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierOutstandingDTO {

    private Long supplierId;
    private String supplierName;
    private String supplierEmail;
    private String supplierPhone;
    private BigDecimal outstandingAmount;
    private Integer invoiceCount;
    private Integer overdueInvoiceCount;
    private String paymentTerms;
}