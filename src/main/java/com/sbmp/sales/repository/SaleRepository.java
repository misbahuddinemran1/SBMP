package com.sbmp.sales.repository;

import com.sbmp.business.entity.Business;
import com.sbmp.customer.entity.Customer;
import com.sbmp.sales.entity.Sale;
import com.sbmp.sales.enums.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SaleRepository
        extends JpaRepository<Sale, Long> {

    Optional<Sale> findByIdAndBusiness(
            Long id,
            Business business
    );

    Optional<Sale> findByInvoiceNo(
            String invoiceNo
    );

    boolean existsByInvoiceNo(
            String invoiceNo
    );

    Page<Sale> findByBusiness(
            Business business,
            Pageable pageable
    );

    Page<Sale> findByBusinessAndStatus(
            Business business,
            SaleStatus status,
            Pageable pageable
    );

    Page<Sale> findByBusinessAndCustomer(
            Business business,
            Customer customer,
            Pageable pageable
    );

    Page<Sale> findByBusinessAndSaleDateBetween(
            Business business,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    long countByBusiness(
            Business business
    );
    long countByInvoiceNoStartingWith(String prefix);
}