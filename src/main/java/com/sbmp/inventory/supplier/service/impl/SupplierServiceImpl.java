package com.sbmp.inventory.supplier.service.impl;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.supplier.entity.Supplier;
import com.sbmp.inventory.supplier.repository.SupplierRepository;
import com.sbmp.inventory.supplier.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl
        implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional
    public Supplier save(Supplier supplier) {

        if (supplierRepository
                .existsByPhoneAndBusiness(
                        supplier.getPhone(),
                        supplier.getBusiness()
                )) {

            throw new RuntimeException(
                    "Phone number already exists."
            );
        }

        return supplierRepository.save(supplier);
    }

    @Override
    @Transactional
    public Supplier update(Long id,
                           Supplier updated) {

        Supplier existing =
                getByIdAndBusiness(
                        id,
                        updated.getBusiness()
                );

        if (supplierRepository
                .existsByPhoneAndIdNotAndBusiness(
                        updated.getPhone(),
                        id,
                        updated.getBusiness()
                )) {

            throw new RuntimeException(
                    "Phone number already exists."
            );
        }

        existing.setSupplierName(
                updated.getSupplierName()
        );

        existing.setCompanyName(
                updated.getCompanyName()
        );

        existing.setPhone(
                updated.getPhone()
        );

        existing.setEmail(
                updated.getEmail()
        );

        existing.setAddress(
                updated.getAddress()
        );

        existing.setActive(
                updated.getActive()
        );

        return supplierRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteByIdAndBusiness(
            Long id,
            Business business
    ) {

        Supplier supplier =
                getByIdAndBusiness(
                        id,
                        business
                );

        supplierRepository.delete(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier getByIdAndBusiness(
            Long id,
            Business business
    ) {

        return supplierRepository
                .findByIdAndBusiness(
                        id,
                        business
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Supplier not found."
                        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Supplier> findAll(
            Business business,
            Pageable pageable
    ) {

        return supplierRepository
                .findByBusiness(
                        business,
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Supplier> search(
            Business business,
            String keyword,
            Pageable pageable
    ) {

        return supplierRepository.search(
                business,
                keyword,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal(
            Business business
    ) {

        return supplierRepository
                .countByBusiness(
                        business
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive(
            Business business
    ) {

        return supplierRepository
                .countByBusinessAndActive(
                        business,
                        true
                );
    }
}