package com.sbmp.inventory.category.service;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * eLoan SaaS — CategoryService
 * Service interface for Category business logic
 */
public interface CategoryService {

    // ─────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────

    /**
     * Save new category
     */
    Category save(
            Category category
    );

    /**
     * Update category
     */
    Category update(
            Long id,
            Category category
    );

    /**
     * Find by ID
     */
    Optional<Category> findById(
            Long id
    );

    /**
     * Get by ID
     */
    Category getById(
            Long id
    );

    /**
     * Get by ID and Business
     */
    Category getByIdAndBusiness(
            Long id,
            Business business
    );

    /**
     * Delete category
     */
    void deleteById(
            Long id
    );

    // ─────────────────────────────────────────────
    // LISTING & SEARCH
    // ─────────────────────────────────────────────

    /**
     * Find all categories by business
     */
    Page<Category> findAll(

            Business business,

            Pageable pageable
    );

    /**
     * Search categories business-wise
     */
    Page<Category> search(

            Business business,

            String keyword,

            Boolean active,

            Pageable pageable
    );

    /**
     * Active categories for dropdowns
     */
    List<Category> findAllActive(
            Business business
    );

    // ─────────────────────────────────────────────
    // VALIDATION
    // ─────────────────────────────────────────────

    boolean existsByName(

            String name,

            Business business
    );

    boolean existsByNameAndIdNot(

            String name,

            Long id,

            Business business
    );

    // ─────────────────────────────────────────────
    // STATS
    // ─────────────────────────────────────────────

    long countActive(
            Business business
    );

    long countInactive(
            Business business
    );

    long countTotal(
            Business business
    );
}