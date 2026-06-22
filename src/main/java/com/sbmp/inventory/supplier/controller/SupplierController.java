package com.sbmp.inventory.supplier.controller;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.supplier.dto.request.CreateSupplierRequest;
import com.sbmp.inventory.supplier.dto.request.UpdateSupplierRequest;
import com.sbmp.inventory.supplier.entity.Supplier;
import com.sbmp.inventory.supplier.service.SupplierService;
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
@RequestMapping("/inventory/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final UserRepository userRepository;

    private static final String LIST_VIEW =
            "inventory/supplier/list";

    private static final String FORM_VIEW =
            "inventory/supplier/form";

    private static final String VIEW_VIEW =
            "inventory/supplier/view";

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
    // LIST PAGE
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

        Page<Supplier> suppliers =
                supplierService.search(
                        business,
                        keyword,
                        pageable
                );

        model.addAttribute(
                "suppliers",
                suppliers
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "totalSuppliers",
                supplierService.countTotal(
                        business
                )
        );

        model.addAttribute(
                "activeSuppliers",
                supplierService.countActive(
                        business
                )
        );

        model.addAttribute(
                "activePage",
                "supplier"
        );

        return LIST_VIEW;
    }

    // ─────────────────────────────────────────────────────────────
    // ADD FORM
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/add")
    public String addForm(
            Model model
    ) {

        model.addAttribute(
                "supplier",
                new CreateSupplierRequest()
        );

        model.addAttribute(
                "pageTitle",
                "Add Supplier"
        );

        model.addAttribute(
                "isEdit",
                false
        );

        model.addAttribute(
                "activePage",
                "supplier"
        );

        return FORM_VIEW;
    }

    // ─────────────────────────────────────────────────────────────
    // SAVE
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/save")
    public String save(

            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @ModelAttribute("supplier")
            CreateSupplierRequest request,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "pageTitle",
                    "Add Supplier"
            );

            model.addAttribute(
                    "isEdit",
                    false
            );

            return FORM_VIEW;
        }

        Supplier supplier = Supplier.builder()

                .supplierName(
                        request.getSupplierName()
                )

                .companyName(
                        request.getCompanyName()
                )

                .phone(
                        request.getPhone()
                )

                .email(
                        request.getEmail()
                )

                .address(
                        request.getAddress()
                )

                .active(
                        request.getActive()
                )

                .business(
                        business
                )

                .build();

        supplierService.save(supplier);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Supplier created successfully."
        );

        return "redirect:/inventory/supplier";
    }

    // ─────────────────────────────────────────────────────────────
    // VIEW
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

        Supplier supplier =
                supplierService.getByIdAndBusiness(
                        id,
                        business
                );

        model.addAttribute(
                "supplier",
                supplier
        );

        model.addAttribute(
                "activePage",
                "supplier"
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

        Supplier supplier =
                supplierService.getByIdAndBusiness(
                        id,
                        business
                );

        UpdateSupplierRequest request =
                new UpdateSupplierRequest();

        request.setSupplierName(
                supplier.getSupplierName()
        );

        request.setCompanyName(
                supplier.getCompanyName()
        );

        request.setPhone(
                supplier.getPhone()
        );

        request.setEmail(
                supplier.getEmail()
        );

        request.setAddress(
                supplier.getAddress()
        );

        request.setActive(
                supplier.getActive()
        );

        model.addAttribute(
                "supplier",
                request
        );

        model.addAttribute(
                "supplierId",
                supplier.getId()
        );

        model.addAttribute(
                "pageTitle",
                "Edit Supplier"
        );

        model.addAttribute(
                "isEdit",
                true
        );

        model.addAttribute(
                "activePage",
                "supplier"
        );

        return FORM_VIEW;
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/update")
    public String update(

            @PathVariable
            Long id,

            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @ModelAttribute("supplier")
            UpdateSupplierRequest request,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        Supplier existing =
                supplierService.getByIdAndBusiness(
                        id,
                        business
                );

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "pageTitle",
                    "Edit Supplier"
            );

            model.addAttribute(
                    "isEdit",
                    true
            );

            model.addAttribute(
                    "supplierId",
                    id
            );

            return FORM_VIEW;
        }

        existing.setSupplierName(
                request.getSupplierName()
        );

        existing.setCompanyName(
                request.getCompanyName()
        );

        existing.setPhone(
                request.getPhone()
        );

        existing.setEmail(
                request.getEmail()
        );

        existing.setAddress(
                request.getAddress()
        );

        existing.setActive(
                request.getActive()
        );

        supplierService.update(
                id,
                existing
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Supplier updated successfully."
        );

        return "redirect:/inventory/supplier";
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE
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

        supplierService.deleteByIdAndBusiness(
                id,
                business
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Supplier deleted successfully."
        );

        return "redirect:/inventory/supplier";
    }
}