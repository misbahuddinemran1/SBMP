package com.sbmp.customer.repository;

import com.sbmp.business.entity.Business;
import com.sbmp.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {


    List<Customer> findAllByBusinessAndActiveTrueOrderByNameAsc(Business business);
    

    Optional<Customer> findByIdAndBusiness(
            Long id,
            Business business
    );

    boolean existsByCustomerCode(
            String customerCode
    );

    long countByBusiness(
            Business business
    );
    
    long countByBusinessAndActiveTrue(Business business);

    long countByBusinessAndActive(
            Business business,
            Boolean active
    );

    @Query("""
        SELECT c
        FROM Customer c
        WHERE c.business = :business

        AND (
            :keyword IS NULL
            OR :keyword = ''
            OR LOWER(c.name)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.mobile)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.customerCode)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.email)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
        )

        AND (
            :customerType IS NULL
            OR :customerType = ''
            OR c.customerType = :customerType
        )

        AND (
            :source IS NULL
            OR :source = ''
            OR c.source = :source
        )

        AND (
            :active IS NULL
            OR c.active = :active
        )
    """)
    Page<Customer> search(

            @Param("business")
            Business business,

            @Param("keyword")
            String keyword,

            @Param("customerType")
            String customerType,

            @Param("source")
            String source,

            @Param("active")
            Boolean active,

            Pageable pageable
    );
}