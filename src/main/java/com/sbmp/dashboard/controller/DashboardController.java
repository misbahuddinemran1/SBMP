package com.sbmp.dashboard.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

//    @GetMapping("/dashboard")
//    public String dashboard() {
//        return "dashboard/dashboard";
//    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("businessName", "My Business Ltd.");
        model.addAttribute("userName", "Admin User");
        model.addAttribute("userEmail", "admin@eloan.com");
        model.addAttribute("userRole", "Administrator");
        model.addAttribute("todaySales", 18450);
        model.addAttribute("monthlyRevenue", 384200);
        model.addAttribute("totalProducts", 248);
        model.addAttribute("lowStockCount", 14);
        return "dashboard/dashboard";
    }
}