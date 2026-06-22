package com.sbmp.sales.controller;

import com.sbmp.business.entity.Business;
import com.sbmp.customer.repository.CustomerRepository;
import com.sbmp.inventory.product.repository.ProductRepository;
import com.sbmp.sales.dto.SaleRequestDto;
import com.sbmp.sales.dto.SaleResponseDto;
import com.sbmp.sales.enums.SaleStatus;
import com.sbmp.sales.service.SaleService;
import com.sbmp.user.entity.User;
import com.sbmp.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService        saleService;
    private final CustomerRepository customerRepository;
    private final ProductRepository  productRepository;
    private final UserRepository     userRepository;

    // ─────────────────────────────────────────────
    // GET : New Sale Page
    // ─────────────────────────────────────────────
    @GetMapping("/add")
    public String addSalePage(

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        var customers =
                customerRepository
                        .findAllByBusinessAndActiveTrueOrderByNameAsc(
                                business
                        );

        var products =
                productRepository
                        .findByBusiness(business);

        SaleRequestDto sale = new SaleRequestDto();
        sale.setSaleDate(LocalDate.now());
        sale.setBusinessId(business.getId());
        sale.setStatus(SaleStatus.DRAFT);

        model.addAttribute("sale",       sale);
        model.addAttribute("customers",  customers);
        model.addAttribute("products",   products);
        model.addAttribute("businessId", business.getId());
        model.addAttribute("businessName", business.getBusinessName()); // getName() নয়, getBusinessName()

        return "sales/form";
    }

    // ─────────────────────────────────────────────
    // POST : Save Sale
    // ─────────────────────────────────────────────
    @PostMapping("/add")
    public String saveSale(

            @Valid
            @ModelAttribute("sale")
            SaleRequestDto dto,

            BindingResult result,

            @AuthenticationPrincipal
            UserDetails userDetails,

            RedirectAttributes ra
    ) {

        // ── Validation errors ────────────────────
        if (result.hasErrors()) {

            String errors = result.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            ra.addFlashAttribute("error", errors);
            return "redirect:/sales/add";
        }

        // ── Set businessId from session ──────────
        Business business =
                getCurrentBusiness(userDetails);

        dto.setBusinessId(business.getId());

        try {

            SaleResponseDto saved =
                    saleService.createSale(dto);

            ra.addFlashAttribute(
                    "success",
                    "Invoice " + saved.getInvoiceNo()
                            + " created successfully!"
            );

            return "redirect:/sales/" + saved.getId();

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "error",
                    "Something went wrong: " + e.getMessage()
            );

            return "redirect:/sales/add";
        }
    }

    // ─────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────
    private Business getCurrentBusiness(
            UserDetails userDetails
    ) {

        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return user.getBusiness();
    }
}