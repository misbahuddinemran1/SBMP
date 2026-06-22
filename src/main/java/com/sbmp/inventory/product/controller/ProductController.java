package com.sbmp.inventory.product.controller;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.category.entity.Category;
import com.sbmp.inventory.category.service.CategoryService;
import com.sbmp.inventory.product.dto.request.CreateProductRequest;
import com.sbmp.inventory.product.dto.request.UpdateProductRequest;
import com.sbmp.inventory.product.entity.Product;
import com.sbmp.inventory.product.service.ProductService;
import com.sbmp.user.entity.User;
import com.sbmp.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventory/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    private static final String LIST_VIEW =
            "inventory/product/list";

    private static final String FORM_VIEW =
            "inventory/product/form";

    private static final String VIEW_VIEW =
            "inventory/product/view";

    // ─────────────────────────────────────────────────────────────
    // CURRENT BUSINESS
    // ─────────────────────────────────────────────────────────────

    private Business getCurrentBusiness(
            UserDetails userDetails
    ) {

        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow();

        return user.getBusiness();
    }

    // ─────────────────────────────────────────────────────────────
    // PRODUCT LIST
    // ─────────────────────────────────────────────────────────────

    @GetMapping
    public String list(

            @AuthenticationPrincipal
            UserDetails userDetails,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("createdAt")
                                .descending()
                );

        Page<Product> products =
                productService.search(
                        business,
                        keyword,
                        pageable
                );

        model.addAttribute(
                "products",
                products
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "categories",
                categoryService.findAllActive(
                        business
                )
        );

        model.addAttribute(
                "totalProducts",
                productService.countTotal(
                        business
                )
        );

        model.addAttribute(
                "lowStockCount",
                productService.countLowStock(
                        business
                )
        );

        model.addAttribute(
                "activePage",
                "inventory"
        );

        return LIST_VIEW;
    }

    // ─────────────────────────────────────────────────────────────
    // ADD FORM
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/add")
    public String addForm(

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        model.addAttribute(
                "categories",

                categoryService.findAllActive(
                        business
                )
        );

        model.addAttribute(
                "product",
                new CreateProductRequest()
        );

        model.addAttribute(
                "pageTitle",
                "Add Product"
        );

        model.addAttribute(
                "isEdit",
                false
        );

        model.addAttribute(
                "activePage",
                "inventory"
        );

        return FORM_VIEW;
    }

    // ─────────────────────────────────────────────────────────────
    // SAVE PRODUCT
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/save")
    public String save(

            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @ModelAttribute("product")
            CreateProductRequest request,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "categories",

                    categoryService.findAllActive(
                            business
                    )
            );

            model.addAttribute(
                    "pageTitle",
                    "Add Product"
            );

            model.addAttribute(
                    "isEdit",
                    false
            );

            return FORM_VIEW;
        }

        Category category =
                categoryService
                        .getByIdAndBusiness(
                                request.getCategoryId(),
                                business
                        );

        Product product = Product.builder()

                .name(request.getName())

                .sku(request.getSku())

                .barcode(request.getBarcode())

                .description(
                        request.getDescription()
                )

                .purchasePrice(
                        request.getPurchasePrice()
                )

                .sellingPrice(
                        request.getSellingPrice()
                )

                .stockQuantity(
                        request.getStockQuantity()
                )

                .minimumStockAlert(
                        request.getMinimumStockAlert()
                )

                .unit(request.getUnit())

                .active(request.getActive())

                .category(category)

                .business(business)

                .build();

        productService.save(product);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product created successfully."
        );

        return "redirect:/inventory/product";
    }

    // ─────────────────────────────────────────────────────────────
    // VIEW PRODUCT
    // ─────────────────────────────────────────────────────────────

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

        Product product =
                productService.getByIdAndBusiness(
                        id,
                        business
                );

        model.addAttribute(
                "product",
                product
        );

        model.addAttribute(
                "activePage",
                "inventory"
        );

        return VIEW_VIEW;
    }

    // ─────────────────────────────────────────────────────────────
    // EDIT FORM
    // ─────────────────────────────────────────────────────────────

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

        Product product =
                productService.getByIdAndBusiness(
                        id,
                        business
                );

        UpdateProductRequest request =
                new UpdateProductRequest();

        request.setName(product.getName());

        request.setSku(product.getSku());

        request.setBarcode(product.getBarcode());

        request.setDescription(
                product.getDescription()
        );

        request.setPurchasePrice(
                product.getPurchasePrice()
        );

        request.setSellingPrice(
                product.getSellingPrice()
        );

        request.setStockQuantity(
                product.getStockQuantity()
        );

        request.setMinimumStockAlert(
                product.getMinimumStockAlert()
        );

        request.setUnit(
                product.getUnit()
        );

        request.setActive(
                product.getActive()
        );

        request.setCategoryId(
                product.getCategory().getId()
        );

        model.addAttribute(
                "product",
                request
        );

        model.addAttribute(
                "productId",
                product.getId()
        );

        model.addAttribute(
                "categories",

                categoryService.findAllActive(
                        business
                )
        );

        model.addAttribute(
                "pageTitle",
                "Edit Product"
        );

        model.addAttribute(
                "isEdit",
                true
        );

        model.addAttribute(
                "activePage",
                "inventory"
        );

        return FORM_VIEW;
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE PRODUCT
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/update")
    public String update(

            @PathVariable
            Long id,

            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @ModelAttribute("product")
            UpdateProductRequest request,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        Product existing =
                productService.getByIdAndBusiness(
                        id,
                        business
                );

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "categories",

                    categoryService.findAllActive(
                            business
                    )
            );

            model.addAttribute(
                    "pageTitle",
                    "Edit Product"
            );

            model.addAttribute(
                    "isEdit",
                    true
            );

            model.addAttribute(
                    "productId",
                    id
            );

            return FORM_VIEW;
        }

        Category category =
                categoryService
                        .getByIdAndBusiness(
                                request.getCategoryId(),
                                business
                        );

        existing.setName(
                request.getName()
        );

        existing.setSku(
                request.getSku()
        );

        existing.setBarcode(
                request.getBarcode()
        );

        existing.setDescription(
                request.getDescription()
        );

        existing.setPurchasePrice(
                request.getPurchasePrice()
        );

        existing.setSellingPrice(
                request.getSellingPrice()
        );

        existing.setStockQuantity(
                request.getStockQuantity()
        );

        existing.setMinimumStockAlert(
                request.getMinimumStockAlert()
        );

        existing.setUnit(
                request.getUnit()
        );

        existing.setActive(
                request.getActive()
        );

        existing.setCategory(
                category
        );

        productService.update(
                id,
                existing
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product updated successfully."
        );

        return "redirect:/inventory/product";
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE PRODUCT
    // ─────────────────────────────────────────────────────────────

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

        productService.deleteByIdAndBusiness(
                id,
                business
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product deleted successfully."
        );

        return "redirect:/inventory/product";
    }
}