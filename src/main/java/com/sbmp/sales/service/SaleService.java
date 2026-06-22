package com.sbmp.sales.service;

import com.sbmp.sales.dto.SaleRequestDto;
import com.sbmp.sales.dto.SaleResponseDto;

public interface SaleService {

    SaleResponseDto createSale(SaleRequestDto dto);
}