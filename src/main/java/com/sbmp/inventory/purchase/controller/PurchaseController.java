package com.sbmp.inventory.purchase.controller;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.category.repository.CategoryRepository;
import com.sbmp.inventory.product.repository.ProductRepository;
import com.sbmp.inventory.purchase.dto.PurchaseRequestDTO;
import com.sbmp.inventory.purchase.entity.Purchase;
import com.sbmp.inventory.purchase.repository.PurchaseRepository;
import com.sbmp.inventory.purchase.service.PurchaseService;
import com.sbmp.inventory.supplier.dto.SupplierDropdownDTO;
import com.sbmp.inventory.supplier.repository.SupplierRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

@Controller
@RequestMapping("/inventory/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService     purchaseService;
    private final SupplierRepository  supplierRepository;
    private final CategoryRepository  categoryRepository;
    private final ProductRepository   productRepository;
    private final PurchaseRepository  purchaseRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────
    // GET : Dashboard
    // ─────────────────────────────────────────────
    @GetMapping

    public String dashboard(

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);
        Long bid = business.getId();

        var allPurchases = purchaseRepository
                .findByBusiness_IdOrderByCreatedAtDesc(bid);

        BigDecimal totalPurchase = allPurchases.stream()
                .map(p -> p.getGrandTotal() != null ? p.getGrandTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = allPurchases.stream()
                .map(p -> p.getPaidAmount() != null ? p.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDue = allPurchases.stream()
                .map(p -> p.getDueAmount() != null ? p.getDueAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today        = LocalDate.now();
        LocalDate thisMonStart = today.withDayOfMonth(1);
        LocalDate lastMonStart = thisMonStart.minusMonths(1);
        LocalDate lastMonEnd   = thisMonStart.minusDays(1);

        BigDecimal thisMonthTotal = purchaseRepository
                .getPurchaseTotalBetween(bid, thisMonStart, today);
        BigDecimal lastMonthTotal = purchaseRepository
                .getPurchaseTotalBetween(bid, lastMonStart, lastMonEnd);

        LocalDate sixMonthsAgo = today.minusMonths(5).withDayOfMonth(1);
        List<Object[]> trendRaw = purchaseRepository
                .getMonthlyPurchaseTrend(bid, sixMonthsAgo);

        List<String>     trendLabels = new ArrayList<>();
        List<BigDecimal> trendValues = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate m  = today.minusMonths(i);
            int monthNum = m.getMonthValue();
            int yearNum  = m.getYear();
            trendLabels.add(m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + yearNum);
            trendValues.add(trendRaw.stream()
                    .filter(r -> ((Number) r[0]).intValue() == monthNum && ((Number) r[1]).intValue() == yearNum)
                    .map(r -> (BigDecimal) r[2]).findFirst().orElse(BigDecimal.ZERO));
        }

        List<Object[]> paymentRaw = purchaseRepository.getPaymentMethodBreakdown(bid);
        List<String>     payLabels = new ArrayList<>();
        List<BigDecimal> payValues = new ArrayList<>();
        paymentRaw.forEach(r -> { payLabels.add(r[0].toString()); payValues.add((BigDecimal) r[1]); });

        List<Object[]> supplierRaw = purchaseRepository.getTopSuppliers(bid, PageRequest.of(0, 5));
        List<String>     supLabels = new ArrayList<>();
        List<BigDecimal> supValues = new ArrayList<>();
        supplierRaw.forEach(r -> { supLabels.add(r[0].toString()); supValues.add((BigDecimal) r[1]); });

        List<Object[]> productRaw = purchaseRepository.getTopProducts(bid, PageRequest.of(0, 5));
        List<String>     prodLabels = new ArrayList<>();
        List<BigDecimal> prodValues = new ArrayList<>();
        productRaw.forEach(r -> { prodLabels.add(r[0].toString()); prodValues.add((BigDecimal) r[1]); });

        List<Purchase> draftInvoices = purchaseRepository
                .findByBusiness_IdAndStatusOrderByCreatedAtDesc(bid, Purchase.PurchaseStatus.DRAFT);

        List<Purchase> recentPurchases = allPurchases.stream()
                .limit(10).collect(Collectors.toList());

        var lowStockProducts = productRepository.findLowStockProducts(business);

        model.addAttribute("totalSuppliers",   supplierRepository.countByBusiness(business));
        model.addAttribute("totalProducts",    productRepository.countByBusiness(business));
        model.addAttribute("totalCategories",  categoryRepository.countByBusiness(business));
        model.addAttribute("totalInvoices",    allPurchases.size());
        model.addAttribute("totalPurchase",    totalPurchase);
        model.addAttribute("totalPaid",        totalPaid);
        model.addAttribute("totalDue",         totalDue);
        model.addAttribute("thisMonthTotal",   thisMonthTotal != null ? thisMonthTotal : BigDecimal.ZERO);
        model.addAttribute("lastMonthTotal",   lastMonthTotal != null ? lastMonthTotal : BigDecimal.ZERO);
        model.addAttribute("trendLabels",      toJson(trendLabels));
        model.addAttribute("trendValues",      toJson(trendValues));
        model.addAttribute("payLabels",        toJson(payLabels));
        model.addAttribute("payValues",        toJson(payValues));
        model.addAttribute("supLabels",        toJson(supLabels));
        model.addAttribute("supValues",        toJson(supValues));
        model.addAttribute("prodLabels",       toJson(prodLabels));
        model.addAttribute("prodValues",       toJson(prodValues));
        model.addAttribute("recentPurchases",  recentPurchases);
        model.addAttribute("draftInvoices",    draftInvoices);
        model.addAttribute("lowStockProducts", lowStockProducts);

        return "inventory/Purchase/dashboard";
    }

    // ─────────────────────────────────────────────
    // GET : List Page
    // ─────────────────────────────────────────────
    @GetMapping("/list")
    public String listPurchases(

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        model.addAttribute(
                "purchases",
                purchaseService.getAllByBusiness(
                        business.getId()
                )
        );

        return "inventory/Purchase/list";
    }

    // ─────────────────────────────────────────────
    // GET : View (Detail Page)  ← NEW
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public String viewPurchase(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("purchase", purchaseService.getPurchaseById(id));
            return "inventory/Purchase/view";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Purchase not found: " + e.getMessage());
            return "redirect:/inventory/purchase/list";
        }
    }

    // ─────────────────────────────────────────────
    // GET : Edit Form  ← NEW
    // ─────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editPurchasePage(

            @PathVariable Long id,

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model,

            RedirectAttributes ra
    ) {

        try {

            Business business =
                    getCurrentBusiness(userDetails);

            List<SupplierDropdownDTO> suppliers =
                    supplierRepository
                            .findByBusinessAndActiveTrue(business)
                            .stream()
                            .map(SupplierDropdownDTO::from)
                            .collect(Collectors.toList());

            model.addAttribute(
                    "purchase",
                    purchaseService.getPurchaseById(id)
            );

            model.addAttribute(
                    "businessId",
                    business.getId()
            );

            model.addAttribute(
                    "suppliers",
                    suppliers
            );

            model.addAttribute(
                    "categories",
                    categoryRepository
                            .findByBusinessAndActiveTrueOrderByNameAsc(business)
            );

            model.addAttribute(
                    "products",
                    productRepository.findByBusiness(business)
            );

            return "inventory/Purchase/edit";

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "error",
                    "Purchase not found: " + e.getMessage()
            );

            return "redirect:/inventory/purchase/list";
        }
    }

    // ─────────────────────────────────────────────
    // POST : Update  ← NEW
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/update")
    public String updatePurchase(
            @PathVariable Long id,
            @Valid @ModelAttribute PurchaseRequestDTO dto,
            BindingResult result,
            RedirectAttributes ra
    ) {
        if (result.hasErrors()) {
            String errors = result.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage()).collect(Collectors.joining(", "));
            ra.addFlashAttribute("error", errors);
            return "redirect:/inventory/purchase/" + id + "/edit";
        }

        try {
            // COMPLETED purchase ও edit করার জন্য আগে DRAFT করা হচ্ছে
            Purchase purchase = purchaseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Purchase not found"));
            if (purchase.getStatus() == Purchase.PurchaseStatus.COMPLETED) {
                purchase.setStatus(Purchase.PurchaseStatus.DRAFT);
                purchaseRepository.save(purchase);
            }

            purchaseService.updatePurchase(id, dto);
            ra.addFlashAttribute("success", "Purchase updated successfully!");
            return "redirect:/inventory/purchase/" + id;

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Update failed: " + e.getMessage());
            return "redirect:/inventory/purchase/" + id + "/edit";
        }
    }

    // ─────────────────────────────────────────────
    // POST : Cancel  ← NEW
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/cancel")
    public String cancelPurchase(@PathVariable Long id, RedirectAttributes ra) {
        try {
            purchaseService.cancelPurchase(id);
            ra.addFlashAttribute("success", "Purchase cancelled successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Cancel failed: " + e.getMessage());
        }
        return "redirect:/inventory/purchase/" + id;
    }

    // ─────────────────────────────────────────────
    // GET : New Purchase Page
    // ─────────────────────────────────────────────

    @GetMapping("/new")
    public String newPurchasePage(

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        List<SupplierDropdownDTO> suppliers =
                supplierRepository
                        .findByBusinessAndActiveTrue(business)
                        .stream()
                        .map(SupplierDropdownDTO::from)
                        .collect(Collectors.toList());

        var categories =
                categoryRepository
                        .findByBusinessAndActiveTrueOrderByNameAsc(business);

        var products =
                productRepository.findByBusiness(business);

        System.out.println("Business ID = " + business.getId());
        System.out.println("Suppliers = " + suppliers.size());
        System.out.println("Categories = " + categories.size());
        System.out.println("Products = " + products.size());

        model.addAttribute("businessId", business.getId());
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);

        return "inventory/Purchase/form";
    }

    // ─────────────────────────────────────────────
    // POST : Save Purchase
    // ─────────────────────────────────────────────
    @PostMapping("/save")
    public String savePurchase(
            @Valid @ModelAttribute PurchaseRequestDTO dto,
            BindingResult result,
            RedirectAttributes ra
    ) {
        if (result.hasErrors()) {
            System.out.println("========== VALIDATION ERRORS ==========");
            result.getAllErrors().forEach(e -> System.out.println(e.toString()));
            System.out.println("======================================");

            String errors = result.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage()).collect(Collectors.joining(", "));
            ra.addFlashAttribute("error", errors);
            return "redirect:/inventory/purchase/new";
        }

        try {
            var saved = purchaseService.createPurchase(dto);
            ra.addFlashAttribute("success", "Purchase saved successfully!");
            return "redirect:/inventory/purchase/" + saved.getId();

        } catch (Exception e) {
            System.out.println("========== SERVICE ERROR ==========");
            e.printStackTrace();
            System.out.println("===================================");
            ra.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            return "redirect:/inventory/purchase/new";
        }
    }

    // ─────────────────────────────────────────────
    // HELPER : JSON builder
    // ─────────────────────────────────────────────
    private String toJson(List<?> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item instanceof String)
                sb.append("\"").append(item.toString().replace("\"", "\\\"")).append("\"");
            else sb.append(item);
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    // ─────────────────────────────────────────────
    // TEMP : Replace with Spring Security later
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
