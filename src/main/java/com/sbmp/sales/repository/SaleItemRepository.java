package com.sbmp.sales.repository;

import com.sbmp.sales.entity.Sale;
import com.sbmp.sales.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleItemRepository
        extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findBySale(
            Sale sale
    );

    void deleteBySale(
            Sale sale
    );
}