package com.sbmp.customer.mapper;

import com.sbmp.customer.dto.CustomerCreateDto;
import com.sbmp.customer.dto.CustomerUpdateDto;
import com.sbmp.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(
            CustomerCreateDto dto
    ) {

        return Customer.builder()
                .name(dto.getName())
                .mobile(dto.getMobile())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .customerType(dto.getCustomerType())
                .source(dto.getSource())
                .active(dto.getActive())
                .build();
    }

    public void updateEntity(
            Customer customer,
            CustomerUpdateDto dto
    ) {

        customer.setName(
                dto.getName()
        );

        customer.setMobile(
                dto.getMobile()
        );

        customer.setEmail(
                dto.getEmail()
        );

        customer.setAddress(
                dto.getAddress()
        );

        customer.setCustomerType(
                dto.getCustomerType()
        );

        customer.setSource(
                dto.getSource()
        );

        customer.setActive(
                dto.getActive()
        );
    }
}