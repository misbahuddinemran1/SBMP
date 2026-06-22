package com.sbmp.inventory.category.service.impl;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.category.entity.Category;
import com.sbmp.inventory.category.exception.DuplicateResourceException;
import com.sbmp.inventory.category.exception.ResourceNotFoundException;
import com.sbmp.inventory.category.repository.CategoryRepository;
import com.sbmp.inventory.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * eLoan SaaS — CategoryServiceImpl
 * Implementation of CategoryService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl
        implements CategoryService {

    private final CategoryRepository categoryRepository;

    // ─────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public Category save(
            Category category
    ) {

        String name =
                category.getName().trim();

        if (categoryRepository
                .existsByNameIgnoreCaseAndBusiness(
                        name,
                        category.getBusiness()
                )) {

            throw new DuplicateResourceException(
                    "Category with name '"
                            + name
                            + "' already exists."
            );
        }

        category.setName(name);

        if (category.getActive() == null) {
            category.setActive(true);
        }

        Category saved =
                categoryRepository.save(category);

        log.info(
                "Category created: id={}, name={}",
                saved.getId(),
                saved.getName()
        );

        return saved;
    }

    @Override
    @Transactional
    public Category update(

            Long id,

            Category updated
    ) {

        Category existing =
                getByIdAndBusiness(
                        id,
                        updated.getBusiness()
                );

        String newName =
                updated.getName().trim();

        if (categoryRepository
                .existsByNameIgnoreCaseAndIdNotAndBusiness(
                        newName,
                        id,
                        updated.getBusiness()
                )) {

            throw new DuplicateResourceException(
                    "Category with name '"
                            + newName
                            + "' already exists."
            );
        }

        existing.setName(newName);

        existing.setDescription(
                updated.getDescription()
        );

        existing.setActive(
                updated.getActive() != null
                        ? updated.getActive()
                        : existing.getActive()
        );

        Category saved =
                categoryRepository.save(existing);

        log.info(
                "Category updated: id={}, name={}",
                saved.getId(),
                saved.getName()
        );

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(
            Long id
    ) {

        return categoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(
            Long id
    ) {

        return categoryRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: "
                                        + id
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Category getByIdAndBusiness(

            Long id,

            Business business
    ) {

        return categoryRepository
                .findByIdAndBusiness(
                        id,
                        business
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: "
                                        + id
                        )
                );
    }

    @Override
    @Transactional
    public void deleteById(
            Long id
    ) {

        Category category =
                getById(id);

        categoryRepository.delete(category);

        log.info(
                "Category deleted: id={}, name={}",
                id,
                category.getName()
        );
    }

    // ─────────────────────────────────────────────
    // LISTING & SEARCH
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<Category> findAll(

            Business business,

            Pageable pageable
    ) {

        return categoryRepository
                .findByBusiness(
                        business,
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> search(

            Business business,

            String keyword,

            Boolean active,

            Pageable pageable
    ) {

        String kw =
                (keyword != null
                        && !keyword.isBlank())
                        ? keyword.trim()
                        : null;

        return categoryRepository.search(
                business,
                kw,
                active,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAllActive(
            Business business
    ) {

        return categoryRepository
                .findByBusinessAndActiveTrueOrderByNameAsc(
                        business
                );
    }

    // ─────────────────────────────────────────────
    // VALIDATION
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(

            String name,

            Business business
    ) {

        return categoryRepository
                .existsByNameIgnoreCaseAndBusiness(
                        name,
                        business
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndIdNot(

            String name,

            Long id,

            Business business
    ) {

        return categoryRepository
                .existsByNameIgnoreCaseAndIdNotAndBusiness(
                        name,
                        id,
                        business
                );
    }

    // ─────────────────────────────────────────────
    // STATS
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long countActive(
            Business business
    ) {

        return categoryRepository
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

        return categoryRepository
                .countByBusinessAndActive(
                        business,
                        false
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal(
            Business business
    ) {

        return categoryRepository
                .countByBusiness(
                        business
                );
    }
}