package com.sbmp.sales.service;

import com.sbmp.sales.dto.SaleRequestDto;
import com.sbmp.sales.dto.SaleResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface SaleService {

    SaleResponseDto createSale(SaleRequestDto dto);

    long countByBusiness(Long businessId);

    BigDecimal getTotalSale(Long businessId);

    BigDecimal getTotalPaid(Long businessId);

    BigDecimal getTotalDue(Long businessId);

    BigDecimal getThisMonthTotal(Long businessId);

    BigDecimal getLastMonthTotal(Long businessId);

    List<SaleResponseDto> getRecentSales(Long businessId, int limit);

    List<SaleResponseDto> getDraftSales(Long businessId);

    List<String> getTrendLabels(Long businessId);

    List<BigDecimal> getTrendValues(Long businessId);

    List<String> getTopProductLabels(Long businessId);

    List<Long> getTopProductValues(Long businessId);

    List<SaleResponseDto> getSalesByBusiness(Long businessId);

    SaleResponseDto getSaleById(Long id);

    SaleRequestDto getSaleForEdit(Long id);

    SaleResponseDto updateSale(Long id, SaleRequestDto dto);

    void cancelSale(Long id);

    void completeSale(Long id);

    void deleteSale(Long id);
}