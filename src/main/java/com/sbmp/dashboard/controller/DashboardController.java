package com.sbmp.dashboard.controller;

import com.sbmp.business.entity.Business;
import com.sbmp.customer.repository.CustomerRepository;
import com.sbmp.inventory.category.repository.CategoryRepository;
import com.sbmp.inventory.product.repository.ProductRepository;
import com.sbmp.inventory.purchase.repository.PurchaseRepository;
import com.sbmp.inventory.stock.repository.StockRepository;
import com.sbmp.inventory.stock.service.StockService;
import com.sbmp.inventory.supplier.repository.SupplierRepository;
import com.sbmp.sales.service.SaleService;
import com.sbmp.user.entity.User;
import com.sbmp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SaleService         saleService;
    private final StockService        stockService;
    private final StockRepository     stockRepository;
    private final ProductRepository   productRepository;
    private final CategoryRepository  categoryRepository;
    private final SupplierRepository  supplierRepository;
    private final CustomerRepository  customerRepository;
    private final PurchaseRepository  purchaseRepository;
    private final UserRepository      userRepository;

    @GetMapping
    public String dashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Business business = getCurrentBusiness(userDetails);
        Long     bid      = business.getId();

        // ── Sales KPIs ──────────────────────────────
        BigDecimal thisMonthSale = saleService.getThisMonthTotal(bid);
        BigDecimal lastMonthSale = saleService.getLastMonthTotal(bid);

        model.addAttribute("totalInvoices",  saleService.countByBusiness(bid));
        model.addAttribute("totalSale",      saleService.getTotalSale(bid));
        model.addAttribute("totalSalePaid",  saleService.getTotalPaid(bid));
        model.addAttribute("totalSaleDue",   saleService.getTotalDue(bid));
        model.addAttribute("thisMonthSale",  thisMonthSale != null ? thisMonthSale : BigDecimal.ZERO);
        model.addAttribute("lastMonthSale",  lastMonthSale != null ? lastMonthSale : BigDecimal.ZERO);
        model.addAttribute("saleTrendPct",   calcTrend(thisMonthSale, lastMonthSale));
        model.addAttribute("recentSales",    saleService.getRecentSales(bid, 8));

        // ── Purchase KPIs ────────────────────────────
        var allPurchases = purchaseRepository
                .findByBusiness_IdOrderByCreatedAtDesc(bid);

        BigDecimal totalPurchase = allPurchases.stream()
                .map(p -> p.getGrandTotal() != null ? p.getGrandTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPurchasePaid = allPurchases.stream()
                .map(p -> p.getPaidAmount() != null ? p.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPurchaseDue = allPurchases.stream()
                .map(p -> p.getDueAmount() != null ? p.getDueAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalPurchase",         totalPurchase);
        model.addAttribute("totalPurchasePaid",     totalPurchasePaid);
        model.addAttribute("totalPurchaseDue",      totalPurchaseDue);
        model.addAttribute("totalPurchaseInvoices", allPurchases.size());
        model.addAttribute("recentPurchases",       allPurchases.stream().limit(8).toList());

        // ── Inventory ────────────────────────────────
        var    lowStocks   = stockService.getLowStocks(bid);
        var    outOfStocks = stockService.getOutOfStocks(bid);
        BigDecimal stockValue = stockRepository.getTotalStockValue(bid);

        model.addAttribute("totalProducts",   productRepository.countByBusiness(business));
        model.addAttribute("totalCategories", categoryRepository.countByBusiness(business));
        model.addAttribute("totalSuppliers",  supplierRepository.countByBusiness(business));
        model.addAttribute("totalStockValue", stockValue != null ? stockValue : BigDecimal.ZERO);
        model.addAttribute("lowStockCount",   lowStocks.size());
        model.addAttribute("outOfStockCount", outOfStocks.size());
        model.addAttribute("lowStocks",       lowStocks.stream().limit(8).toList());

        // ── Customers ────────────────────────────────
        model.addAttribute("totalCustomers",  customerRepository.countByBusiness(business));
        model.addAttribute("activeCustomers", customerRepository.countByBusinessAndActiveTrue(business));

        // ── Common ───────────────────────────────────
        model.addAttribute("activePage",   "dashboard");
        model.addAttribute("businessName", business.getBusinessName());

        return "dashboard/dashboard";
    }

    // percentage change: current vs previous month
    private String calcTrend(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) return "0%";
        if (current  == null) return "0%";
        BigDecimal pct = current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP);
        return (pct.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + pct + "%";
    }

    private Business getCurrentBusiness(UserDetails userDetails) {
        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getBusiness();
    }
}