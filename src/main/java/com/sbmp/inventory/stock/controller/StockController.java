package com.sbmp.inventory.stock.controller;

import com.sbmp.business.entity.Business;
import com.sbmp.inventory.stock.entity.Stock;
import com.sbmp.inventory.stock.entity.StockTransaction;
import com.sbmp.inventory.stock.repository.StockRepository;
import com.sbmp.inventory.stock.service.StockService;
import com.sbmp.user.entity.User;
import com.sbmp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/inventory/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────
    // STOCK LIST
    // ─────────────────────────────────────────────

    @GetMapping
    public String stockList(

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        Business business =
                getCurrentBusiness(userDetails);

        Long bid = business.getId();

        List<Stock> allStocks =
                stockService.getAllStocks(bid);

        List<Stock> lowStocks =
                stockService.getLowStocks(bid);

        List<Stock> outOfStocks =
                stockService.getOutOfStocks(bid);

        BigDecimal totalValue =
                stockRepository.getTotalStockValue(bid);

        model.addAttribute(
                "stocks",
                allStocks
        );

        model.addAttribute(
                "lowStocks",
                lowStocks
        );

        model.addAttribute(
                "outOfStocks",
                outOfStocks
        );

        model.addAttribute(
                "totalValue",
                totalValue != null
                        ? totalValue
                        : BigDecimal.ZERO
        );

        model.addAttribute(
                "totalItems",
                allStocks.size()
        );

        model.addAttribute(
                "lowCount",
                lowStocks.size()
        );

        model.addAttribute(
                "outCount",
                outOfStocks.size()
        );

        return "inventory/stock/stock-list";
    }

    // ─────────────────────────────────────────────
    // STOCK HISTORY
    // ─────────────────────────────────────────────

    @GetMapping("/{productId}/history")
    public String stockHistory(

            @PathVariable
            Long productId,

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model,

            RedirectAttributes ra
    ) {

        try {

            Business business =
                    getCurrentBusiness(
                            userDetails
                    );

            List<StockTransaction> history =
                    stockService.getProductHistory(
                            productId,
                            business.getId()
                    );

            Stock stock =
                    stockService.getStock(
                            productId,
                            business.getId()
                    );

            model.addAttribute(
                    "history",
                    history
            );

            model.addAttribute(
                    "stock",
                    stock
            );

            return "inventory/stock/stock-history";

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "error",
                    "Stock not found : "
                            + e.getMessage()
            );

            return "redirect:/inventory/stock";
        }
    }

    // ─────────────────────────────────────────────
    // MANUAL ADJUSTMENT
    // ─────────────────────────────────────────────

    @PostMapping("/{productId}/adjust")
    public String adjustStock(

            @PathVariable
            Long productId,

            @RequestParam
            int newQuantity,

            @RequestParam(required = false)
            String notes,

            @AuthenticationPrincipal
            UserDetails userDetails,

            RedirectAttributes ra
    ) {

        try {

            Business business =
                    getCurrentBusiness(
                            userDetails
                    );

            stockService.adjustStock(
                    productId,
                    business.getId(),
                    newQuantity,
                    notes
            );

            ra.addFlashAttribute(
                    "success",
                    "Stock adjusted successfully!"
            );

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "error",
                    "Adjustment failed : "
                            + e.getMessage()
            );
        }

        return "redirect:/inventory/stock/"
                + productId
                + "/history";
    }

    // ─────────────────────────────────────────────
    // CURRENT BUSINESS
    // ─────────────────────────────────────────────

    private Business getCurrentBusiness(
            UserDetails userDetails
    ) {

        User user =
                userRepository
                        .findByEmail(
                                userDetails.getUsername()
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "User not found"
                                        )
                        );

        return user.getBusiness();
    }
}