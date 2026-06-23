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
    // GET : Sales Dashboard
    // ─────────────────────────────────────────────
    @GetMapping
    public String salesDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Business business   = getCurrentBusiness(userDetails);
        Long     businessId = business.getId();

        // KPI Cards
        model.addAttribute("totalInvoices",  saleService.countByBusiness(businessId));
        model.addAttribute("totalSale",      saleService.getTotalSale(businessId));
        model.addAttribute("totalPaid",      saleService.getTotalPaid(businessId));
        model.addAttribute("totalDue",       saleService.getTotalDue(businessId));
        model.addAttribute("thisMonthTotal", saleService.getThisMonthTotal(businessId));
        model.addAttribute("lastMonthTotal", saleService.getLastMonthTotal(businessId));
        model.addAttribute("totalCustomers", customerRepository.countByBusinessAndActiveTrue(business));

        // Tables
        model.addAttribute("recentSales",    saleService.getRecentSales(businessId, 10));
        model.addAttribute("draftSales",     saleService.getDraftSales(businessId));

        // Charts
        model.addAttribute("trendLabels",    saleService.getTrendLabels(businessId));
        model.addAttribute("trendValues",    saleService.getTrendValues(businessId));
        model.addAttribute("prodLabels",     saleService.getTopProductLabels(businessId));
        model.addAttribute("prodValues",     saleService.getTopProductValues(businessId));

        return "sales/dashboard";
    }

    // ─────────────────────────────────────────────
    // GET : Sales List
    // ─────────────────────────────────────────────
    @GetMapping("/list")
    public String salesList(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Business business = getCurrentBusiness(userDetails);

        model.addAttribute("sales", saleService.getSalesByBusiness(business.getId()));

        return "sales/list";
    }

    // ─────────────────────────────────────────────
    // GET : New Sale Page
    // ─────────────────────────────────────────────
    @GetMapping("/add")
    public String addSalePage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Business business = getCurrentBusiness(userDetails);

        var customers = customerRepository
                .findAllByBusinessAndActiveTrueOrderByNameAsc(business);

        var products = productRepository
                .findByBusiness(business);

        SaleRequestDto sale = new SaleRequestDto();
        sale.setSaleDate(LocalDate.now());
        sale.setBusinessId(business.getId());
        sale.setStatus(SaleStatus.DRAFT);

        model.addAttribute("sale",         sale);
        model.addAttribute("customers",    customers);
        model.addAttribute("products",     products);
        model.addAttribute("businessId",   business.getId());
        model.addAttribute("businessName", business.getBusinessName());

        return "sales/form";
    }

    // ─────────────────────────────────────────────
    // POST : Save Sale
    // ─────────────────────────────────────────────
    @PostMapping("/add")
    public String saveSale(
            @Valid @ModelAttribute("sale") SaleRequestDto dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra
    ) {
        if (result.hasErrors()) {
            String errors = result.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            ra.addFlashAttribute("error", errors);
            return "redirect:/sales/add";
        }

        Business business = getCurrentBusiness(userDetails);
        dto.setBusinessId(business.getId());

        try {
            SaleResponseDto saved = saleService.createSale(dto);
            ra.addFlashAttribute("success",
                    "Invoice " + saved.getInvoiceNo() + " created successfully!");
            return "redirect:/sales/" + saved.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error",
                    "Something went wrong: " + e.getMessage());
            return "redirect:/sales/add";
        }
    }

    // ─────────────────────────────────────────────
    // GET : Sale Details
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public String saleDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            SaleResponseDto sale = saleService.getSaleById(id);
            model.addAttribute("sale", sale);
            return "sales/view";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Sale not found: " + e.getMessage());
            return "redirect:/sales/list";
        }
    }

    // ─────────────────────────────────────────────
    // GET : Edit Sale Page
    // ─────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editSalePage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes ra
    ) {
        Business business = getCurrentBusiness(userDetails);

        try {
            SaleRequestDto sale = saleService.getSaleForEdit(id);

            var customers = customerRepository
                    .findAllByBusinessAndActiveTrueOrderByNameAsc(business);

            var products = productRepository
                    .findByBusiness(business);

            model.addAttribute("sale",         sale);
            model.addAttribute("customers",    customers);
            model.addAttribute("products",     products);
            model.addAttribute("businessId",   business.getId());
            model.addAttribute("businessName", business.getBusinessName());
            model.addAttribute("saleId",       id);

            return "sales/form";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Sale not found: " + e.getMessage());
            return "redirect:/sales/list";
        }
    }

    // ─────────────────────────────────────────────
    // POST : Update Sale
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/edit")
    public String updateSale(
            @PathVariable Long id,
            @Valid @ModelAttribute("sale") SaleRequestDto dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra
    ) {
        if (result.hasErrors()) {
            String errors = result.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            ra.addFlashAttribute("error", errors);
            return "redirect:/sales/" + id + "/edit";
        }

        Business business = getCurrentBusiness(userDetails);
        dto.setBusinessId(business.getId());

        try {
            SaleResponseDto updated = saleService.updateSale(id, dto);
            ra.addFlashAttribute("success",
                    "Invoice " + updated.getInvoiceNo() + " updated successfully!");
            return "redirect:/sales/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error",
                    "Update failed: " + e.getMessage());
            return "redirect:/sales/" + id + "/edit";
        }
    }

    // ─────────────────────────────────────────────
    // POST : Cancel Sale
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/cancel")
    public String cancelSale(
            @PathVariable Long id,
            RedirectAttributes ra
    ) {
        try {
            saleService.cancelSale(id);
            ra.addFlashAttribute("success", "Sale cancelled successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not cancel: " + e.getMessage());
        }
        return "redirect:/sales/" + id;
    }

    // ─────────────────────────────────────────────
    // POST : Complete Sale
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/complete")
    public String completeSale(
            @PathVariable Long id,
            RedirectAttributes ra
    ) {
        try {
            saleService.completeSale(id);
            ra.addFlashAttribute("success", "Sale completed successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not complete: " + e.getMessage());
        }
        return "redirect:/sales/" + id;
    }

    // ─────────────────────────────────────────────
    // POST : Delete Sale
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String deleteSale(
            @PathVariable Long id,
            RedirectAttributes ra
    ) {
        try {
            saleService.deleteSale(id);
            ra.addFlashAttribute("success", "Sale deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not delete: " + e.getMessage());
        }
        return "redirect:/sales/list";
    }

    // ─────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────
    private Business getCurrentBusiness(UserDetails userDetails) {
        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getBusiness();
    }
}