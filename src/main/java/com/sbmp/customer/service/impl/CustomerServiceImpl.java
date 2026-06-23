package com.sbmp.customer.service.impl;

import com.sbmp.business.entity.Business;
import com.sbmp.customer.dto.CustomerCreateDto;
import com.sbmp.customer.dto.CustomerUpdateDto;
import com.sbmp.customer.entity.Customer;
import com.sbmp.customer.exception.CustomerNotFoundException;
import com.sbmp.customer.mapper.CustomerMapper;
import com.sbmp.customer.repository.CustomerRepository;
import com.sbmp.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl
        implements CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    // ─────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> search(

            Business business,

            String keyword,

            String customerType,

            String source,

            Boolean active,

            Pageable pageable
    ) {

        return customerRepository.search(

                business,

                keyword,

                customerType,

                source,

                active,

                pageable
        );
    }

    // ─────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Customer getByIdAndBusiness(

            Long id,

            Business business
    ) {

        return customerRepository
                .findByIdAndBusiness(
                        id,
                        business
                )
                .orElseThrow(
                        () -> new CustomerNotFoundException(
                                "Customer not found."
                        )
                );
    }

    // ─────────────────────────────────────────────
    // SAVE
    // ─────────────────────────────────────────────

    @Override
    public Customer save(

            Business business,

            CustomerCreateDto dto
    ) {

        Customer customer =
                customerMapper.toEntity(
                        dto
                );

        customer.setBusiness(
                business
        );

        customer.setCustomerCode(
                generateCustomerCode()
        );
        
        customer.setBalance(BigDecimal.ZERO); // ✅ FIX ADDED

        return customerRepository.save(
                customer
        );
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────

    @Override
    public Customer update(

            Long id,

            Business business,

            CustomerUpdateDto dto
    ) {

        Customer customer =
                getByIdAndBusiness(
                        id,
                        business
                );

        customerMapper.updateEntity(
                customer,
                dto
        );

        return customerRepository.save(
                customer
        );
    }

    // ─────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────

    @Override
    public void delete(

            Long id,

            Business business
    ) {

        Customer customer =
                getByIdAndBusiness(
                        id,
                        business
                );

        customerRepository.delete(
                customer
        );
    }

    // ─────────────────────────────────────────────
    // DASHBOARD COUNTS
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long countTotal(
            Business business
    ) {

        return customerRepository
                .countByBusiness(
                        business
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive(
            Business business
    ) {

        return customerRepository
                .countByBusinessAndActive(
                        business,
                        true
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long countInactive(
            Business business
    ) {

        return customerRepository
                .countByBusinessAndActive(
                        business,
                        false
                );
    }

    // ─────────────────────────────────────────────
    // CUSTOMER CODE
    // ─────────────────────────────────────────────

    private String generateCustomerCode() {

        long nextValue =
                customerRepository.count() + 1;

        return String.format(
                "CUS-%05d",
                nextValue
        );
    }
}