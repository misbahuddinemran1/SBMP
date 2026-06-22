package com.sbmp.inventory.category.controller;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.category.entity.Category;
import com.sbmp.inventory.category.exception.DuplicateResourceException;
import com.sbmp.inventory.category.exception.ResourceNotFoundException;
import com.sbmp.inventory.category.service.CategoryService;
import com.sbmp.user.entity.User;
import com.sbmp.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/inventory/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    private final UserRepository userRepository;

    private static final String LIST_VIEW =
            "inventory/category/list";

    private static final String ADD_VIEW =
            "inventory/category/add";

    private static final String VIEW_VIEW =
            "inventory/category/view";

    private static final String REDIRECT_LIST =
            "redirect:/inventory/category";

    // ─────────────────────────────────────────────
    // CURRENT BUSINESS
    // ─────────────────────────────────────────────

    private Business getCurrentBusiness(
            UserDetails userDetails
    ) {

        User user = userRepository
                .findByEmail(
                        userDetails.getUsername()
                )
                .orElseThrow();

        return user.getBusiness();
    }

    // ─────────────────────────────────────────────
    // COMMON MODEL ATTRIBUTES
    // ─────────────────────────────────────────────

    private void addCommonAttributes(

            Model model,

            Business business
    ) {

        model.addAttribute(
                "activePage",
                "inventory"
        );

        model.addAttribute(
                "businessName",
                business.getBusinessName()
        );

        model.addAttribute(
                "totalCount",

                categoryService.countTotal(
                        business
                )
        );

        model.addAttribute(
                "activeCount",

                categoryService.countActive(
                        business
                )
        );

        model.addAttribute(
                "inactiveCount",

                categoryService.countInactive(
                        business
                )
        );
    }

    // ─────────────────────────────────────────────
    // CATEGORY LIST
    // ─────────────────────────────────────────────

    @GetMapping
    public String list(

            @AuthenticationPrincipal
            UserDetails userDetails,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Boolean active,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdAt,desc")
            String sort,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        String[] sortParts =
                sort.split(",");

        Sort.Direction direction =
                sortParts.length > 1 &&
                        sortParts[1]
                                .equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                direction,
                                sortParts[0]
                        )
                );

        Page<Category> categories =
                categoryService.search(

                        business,

                        keyword,

                        active,

                        pageable
                );

        addCommonAttributes(
                model,
                business
        );

        model.addAttribute(
                "categories",
                categories
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "active",
                active
        );

        model.addAttribute(
                "sort",
                sort
        );

        return LIST_VIEW;
    }

    // ─────────────────────────────────────────────
    // ADD FORM
    // ─────────────────────────────────────────────

    @GetMapping("/add")
    public String addForm(

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        addCommonAttributes(
                model,
                business
        );

        model.addAttribute(
                "category",
                new Category()
        );

        model.addAttribute(
                "pageTitle",
                "Add Category"
        );

        return ADD_VIEW;
    }

    // ─────────────────────────────────────────────
    // SAVE CATEGORY
    // ─────────────────────────────────────────────

    @PostMapping("/save")
    public String save(

            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @ModelAttribute("category")
            Category category,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        if (bindingResult.hasErrors()) {

            addCommonAttributes(
                    model,
                    business
            );

            model.addAttribute(
                    "pageTitle",
                    "Add Category"
            );

            return ADD_VIEW;
        }

        try {

            category.setBusiness(
                    business
            );

            categoryService.save(
                    category
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Category created successfully."
            );

            return REDIRECT_LIST;

        } catch (DuplicateResourceException ex) {

            bindingResult.rejectValue(
                    "name",
                    "duplicate",
                    ex.getMessage()
            );

            addCommonAttributes(
                    model,
                    business
            );

            model.addAttribute(
                    "pageTitle",
                    "Add Category"
            );

            return ADD_VIEW;
        }
    }

    // ─────────────────────────────────────────────
    // VIEW CATEGORY
    // ─────────────────────────────────────────────

    @GetMapping("/{id}")
    public String view(

            @PathVariable
            Long id,

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        Category category =
                categoryService
                        .getByIdAndBusiness(
                                id,
                                business
                        );

        addCommonAttributes(
                model,
                business
        );

        model.addAttribute(
                "category",
                category
        );

        return VIEW_VIEW;
    }

    // ─────────────────────────────────────────────
    // EDIT FORM
    // ─────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String editForm(

            @PathVariable
            Long id,

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        Category category =
                categoryService
                        .getByIdAndBusiness(
                                id,
                                business
                        );

        addCommonAttributes(
                model,
                business
        );

        model.addAttribute(
                "category",
                category
        );

        model.addAttribute(
                "pageTitle",
                "Edit Category"
        );

        return ADD_VIEW;
    }

    // ─────────────────────────────────────────────
    // UPDATE CATEGORY
    // ─────────────────────────────────────────────

    @PostMapping("/{id}/update")
    public String update(

            @PathVariable
            Long id,

            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @ModelAttribute("category")
            Category category,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        if (bindingResult.hasErrors()) {

            addCommonAttributes(
                    model,
                    business
            );

            model.addAttribute(
                    "pageTitle",
                    "Edit Category"
            );

            return ADD_VIEW;
        }

        try {

            category.setBusiness(
                    business
            );

            categoryService.update(
                    id,
                    category
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Category updated successfully."
            );

            return REDIRECT_LIST;

        } catch (DuplicateResourceException ex) {

            bindingResult.rejectValue(
                    "name",
                    "duplicate",
                    ex.getMessage()
            );

            addCommonAttributes(
                    model,
                    business
            );

            model.addAttribute(
                    "pageTitle",
                    "Edit Category"
            );

            return ADD_VIEW;

        } catch (ResourceNotFoundException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

            return REDIRECT_LIST;
        }
    }

    // ─────────────────────────────────────────────
    // DELETE CATEGORY
    // ─────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String delete(

            @PathVariable
            Long id,

            @AuthenticationPrincipal
            UserDetails userDetails,

            RedirectAttributes redirectAttributes
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        try {

            Category category =
                    categoryService
                            .getByIdAndBusiness(
                                    id,
                                    business
                            );

            categoryService.deleteById(
                    id
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Category '"
                            + category.getName()
                            + "' deleted successfully."
            );

        } catch (ResourceNotFoundException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return REDIRECT_LIST;
    }
}