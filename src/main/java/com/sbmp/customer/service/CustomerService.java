package com.sbmp.customer.service;

import com.sbmp.business.entity.Business;
import com.sbmp.customer.dto.CustomerCreateDto;
import com.sbmp.customer.dto.CustomerUpdateDto;
import com.sbmp.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    Page<Customer> search(

            Business business,

            String keyword,

            String customerType,

            String source,

            Boolean active,

            Pageable pageable
    );

    Customer getByIdAndBusiness(

            Long id,

            Business business
    );

    Customer save(

            Business business,

            CustomerCreateDto dto
    );

    Customer update(

            Long id,

            Business business,

            CustomerUpdateDto dto
    );

    void delete(

            Long id,

            Business business
    );

    long countTotal(
            Business business
    );

    long countActive(
            Business business
    );

    long countInactive(
            Business business
    );
}