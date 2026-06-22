package com.sbmp.inventory.supplier.repository;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface SupplierRepository
        extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByIdAndBusiness(
            Long id,
            Business business
    );

    boolean existsByPhoneAndBusiness(
            String phone,
            Business business
    );

    boolean existsByPhoneAndIdNotAndBusiness(
            String phone,
            Long id,
            Business business
    );

    Page<Supplier> findByBusiness(
            Business business,
            Pageable pageable
    );

    @Query("""
            SELECT s
            FROM Supplier s
            WHERE s.business = :business
            AND (
                    :keyword IS NULL
                    OR :keyword = ''
                    OR LOWER(s.supplierName)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(s.companyName, ''))
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.phone)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            ORDER BY s.createdAt DESC
            """)
    Page<Supplier> search(
            Business business,
            String keyword,
            Pageable pageable
    );

    long countByBusiness(
            Business business
    );

    long countByBusinessAndActive(
            Business business,
            Boolean active
    );
    
    List<Supplier> findByBusinessAndActiveTrue(
        Business business
);
}