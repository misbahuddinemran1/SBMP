package com.sbmp.inventory.category.repository;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * eLoan SaaS — CategoryRepository
 * Spring Data JPA Repository for Category entity
 */
@Repository
public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    // ─────────────────────────────────────────────
    // SINGLE LOOKUPS
    // ─────────────────────────────────────────────

    Optional<Category> findByIdAndBusiness(

            Long id,

            Business business
    );

    Optional<Category> findByNameIgnoreCaseAndBusiness(

            String name,

            Business business
    );

    // ─────────────────────────────────────────────
    // BUSINESS FILTER
    // ─────────────────────────────────────────────

    Page<Category> findByBusiness(

            Business business,

            Pageable pageable
    );

    // ─────────────────────────────────────────────
    // ACTIVE FILTER
    // ─────────────────────────────────────────────

    List<Category>
    findByBusinessAndActiveTrueOrderByNameAsc(

            Business business
    );

    Page<Category> findByBusinessAndActive(

            Business business,

            Boolean active,

            Pageable pageable
    );

    // ─────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────

    @Query("""
            SELECT c
            FROM Category c
            WHERE c.business = :business
            AND (
                    :keyword IS NULL
                    OR :keyword = ''
                    OR LOWER(c.name)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.description)
                        LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            AND (
                    :active IS NULL
                    OR c.active = :active
                )
            ORDER BY c.createdAt DESC
            """)
    Page<Category> search(

            @Param("business")
            Business business,

            @Param("keyword")
            String keyword,

            @Param("active")
            Boolean active,

            Pageable pageable
    );

    // ─────────────────────────────────────────────
    // EXISTENCE CHECKS
    // ─────────────────────────────────────────────

    boolean existsByNameIgnoreCaseAndBusiness(

            String name,

            Business business
    );

    boolean existsByNameIgnoreCaseAndIdNotAndBusiness(

            String name,

            Long id,

            Business business
    );

    // ─────────────────────────────────────────────
    // COUNTS
    // ─────────────────────────────────────────────

    long countByBusiness(
            Business business
    );

    long countByBusinessAndActive(

            Business business,

            Boolean active
    );
}