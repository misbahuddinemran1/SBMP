package com.sbmp.inventory.supplier.service;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

    Supplier save(Supplier supplier);

    Supplier update(Long id,
                    Supplier supplier);

    void deleteByIdAndBusiness(
            Long id,
            Business business
    );

    Supplier getByIdAndBusiness(
            Long id,
            Business business
    );

    Page<Supplier> findAll(
            Business business,
            Pageable pageable
    );

    Page<Supplier> search(
            Business business,
            String keyword,
            Pageable pageable
    );

    long countTotal(
            Business business
    );

    long countActive(
            Business business
    );
}