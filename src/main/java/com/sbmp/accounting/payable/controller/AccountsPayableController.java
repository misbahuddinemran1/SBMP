package com.sbmp.accounting.payable.controller;

import com.sbmp.accounting.payable.dto.*;
import com.sbmp.accounting.payable.entity.AccountsPayable.APStatus;
import com.sbmp.accounting.payable.service.APService;
import com.sbmp.business.entity.Business;
import com.sbmp.inventory.purchase.repository.PurchaseRepository;
import com.sbmp.inventory.supplier.repository.SupplierRepository;
import com.sbmp.user.entity.User;
import com.sbmp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accounting/payable")
@RequiredArgsConstructor
public class AccountsPayableController {

    private final APService apService;
    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────
    // GET : Dashboard
    // ─────────────────────────────────────────────
    @GetMapping
    public String dashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Business business = getCurrentBusiness(userDetails);
        Long businessId = business.getId();

        // Total Outstanding
        BigDecimal totalOutstanding = apService.getTotalOutstandingAmount(businessId);

        // Overdue AP
        List<AccountsPayableDTO> overdueAP = apService.getOverdueAP(businessId);

        // Aging Report
        List<APAgingReportDTO> agingReport = apService.getAPAgingReport(businessId);

        // Supplier Outstanding Summary
        List<SupplierOutstandingDTO> supplierOutstanding = apService.getSupplierOutstandingSummary(businessId);

        model.addAttribute("totalOutstanding", totalOutstanding);
        model.addAttribute("overdueCount", overdueAP.size());
        model.addAttribute("overdueAmount",
                overdueAP.stream()
                        .map(AccountsPayableDTO::getOutstandingAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        model.addAttribute("overdueAP", overdueAP.stream().limit(10).collect(Collectors.toList()));
        model.addAttribute("agingReport", agingReport);
        model.addAttribute("supplierOutstanding", supplierOutstanding);
        model.addAttribute("totalSuppliers", supplierRepository.count());

        // Aging totals across all suppliers (for dashboard summary cards)
        model.addAttribute("aging0To30",
                agingReport.stream()
                        .map(APAgingReportDTO::getDays0To30)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("aging30To60",
                agingReport.stream()
                        .map(APAgingReportDTO::getDays30To60)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("aging60To90",
                agingReport.stream()
                        .map(APAgingReportDTO::getDays60To90)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("aging90Plus",
                agingReport.stream()
                        .map(APAgingReportDTO::getDays90Plus)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        return "accounting/AP/dashboard";
    }

    // ─────────────────────────────────────────────
    // GET : List Page
    // ─────────────────────────────────────────────
    @GetMapping("/list")
    public String listAP(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) APStatus status,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Business business = getCurrentBusiness(userDetails);

        var apPage = apService.searchAP(
                business.getId(),
                status,
                supplierId,
                null,
                null,
                PageRequest.of(page, size)
        );

        model.addAttribute("apList", apPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", apPage.getTotalPages());
        model.addAttribute("totalElements", apPage.getTotalElements());
        model.addAttribute("statusFilter", status);
        model.addAttribute("supplierFilter", supplierId);
        model.addAttribute("suppliers", supplierRepository.findAll());

        return "accounting/AP/list";
    }

    // ─────────────────────────────────────────────
    // GET : View (Detail Page)
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public String viewAP(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            AccountsPayableDTO apDTO = apService.getAPById(id);
            List<APPaymentScheduleDTO> schedules = apService.getPaymentSchedules(id);

            model.addAttribute("ap", apDTO);
            model.addAttribute("paymentSchedules", schedules);

            return "accounting/AP/view";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "AP not found: " + e.getMessage());
            return "redirect:/accounting/payable/list";
        }
    }

    // ─────────────────────────────────────────────
    // GET : Create AP Form (Select Purchase)
    // ─────────────────────────────────────────────
    @GetMapping("/new")
    public String newAPPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Business business = getCurrentBusiness(userDetails);

        // Get completed purchases that don't have AP yet
        var completedPurchases = purchaseRepository
                .findByBusiness_IdOrderByCreatedAtDesc(business.getId())
                .stream()
                .filter(p -> p.getStatus().toString().equals("COMPLETED"))
                .limit(50)
                .collect(Collectors.toList());

        model.addAttribute("purchases", completedPurchases);
        model.addAttribute("businessId", business.getId());

        return "accounting/AP/form";
    }

    // ─────────────────────────────────────────────
    // POST : Create AP
    // ─────────────────────────────────────────────
    @PostMapping("/save")
    public String createAP(
            @RequestParam Long purchaseId,
            @RequestParam String invoiceNumber,
            @RequestParam LocalDate dueDate,
            RedirectAttributes ra
    ) {
        try {
            AccountsPayableDTO savedAP = apService.createAP(purchaseId, invoiceNumber, dueDate);
            ra.addFlashAttribute("success", "AP created successfully!");
            return "redirect:/accounting/payable/" + savedAP.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to create AP: " + e.getMessage());
            return "redirect:/accounting/payable/new";
        }
    }

    // ─────────────────────────────────────────────
    // GET : Payment Form
    // ─────────────────────────────────────────────
    @GetMapping("/{id}/payment")
    public String paymentPage(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            AccountsPayableDTO apDTO = apService.getAPById(id);
            model.addAttribute("ap", apDTO);
            model.addAttribute("remainingAmount", apDTO.getOutstandingAmount());

            return "accounting/AP/payment";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "AP not found: " + e.getMessage());
            return "redirect:/accounting/payable/list";
        }
    }

    // ─────────────────────────────────────────────
    // POST : Record Payment
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/pay")
    public String recordPayment(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String paymentReference,
            RedirectAttributes ra
    ) {
        try {
            apService.recordPayment(id, amount, paymentReference);
            ra.addFlashAttribute("success", "Payment recorded successfully!");
            return "redirect:/accounting/payable/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Payment failed: " + e.getMessage());
            return "redirect:/accounting/payable/" + id + "/payment";
        }
    }

    // ─────────────────────────────────────────────
    // GET : Payment Schedule Page
    // ─────────────────────────────────────────────
    @GetMapping("/{id}/schedules")
    public String scheduleListPage(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            AccountsPayableDTO apDTO = apService.getAPById(id);
            List<APPaymentScheduleDTO> schedules = apService.getPaymentSchedules(id);

            model.addAttribute("ap", apDTO);
            model.addAttribute("paymentSchedules", schedules);

            return "accounting/AP/schedules";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "AP not found: " + e.getMessage());
            return "redirect:/accounting/payable/list";
        }
    }

    // ─────────────────────────────────────────────
    // POST : Add Payment Schedule
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/schedule/add")
    public String addPaymentSchedule(
            @PathVariable Long id,
            @RequestParam LocalDate scheduledDate,
            @RequestParam BigDecimal scheduledAmount,
            RedirectAttributes ra
    ) {
        try {
            apService.addPaymentSchedule(id, scheduledDate, scheduledAmount);
            ra.addFlashAttribute("success", "Payment schedule added successfully!");
            return "redirect:/accounting/payable/" + id + "/schedules";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to add schedule: " + e.getMessage());
            return "redirect:/accounting/payable/" + id + "/schedules";
        }
    }

    // ─────────────────────────────────────────────
    // GET : Aging Report Page
    // ─────────────────────────────────────────────
    @GetMapping("/reports/aging")
    public String agingReportPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Business business = getCurrentBusiness(userDetails);

        List<APAgingReportDTO> agingReport = apService.getAPAgingReport(business.getId());

        // Calculate totals
        BigDecimal total0To30 = agingReport.stream()
                .map(APAgingReportDTO::getDays0To30)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total30To60 = agingReport.stream()
                .map(APAgingReportDTO::getDays30To60)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total60To90 = agingReport.stream()
                .map(APAgingReportDTO::getDays60To90)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total90Plus = agingReport.stream()
                .map(APAgingReportDTO::getDays90Plus)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("agingReport", agingReport);
        model.addAttribute("total0To30", total0To30);
        model.addAttribute("total30To60", total30To60);
        model.addAttribute("total60To90", total60To90);
        model.addAttribute("total90Plus", total90Plus);
        model.addAttribute("grandTotal", total0To30.add(total30To60).add(total60To90).add(total90Plus));

        return "accounting/AP/report-aging";
    }

    // ─────────────────────────────────────────────
    // GET : Supplier Outstanding Report
    // ─────────────────────────────────────────────
    @GetMapping("/reports/supplier-outstanding")
    public String supplierOutstandingPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Business business = getCurrentBusiness(userDetails);

        List<SupplierOutstandingDTO> supplierOutstanding = apService.getSupplierOutstandingSummary(business.getId());

        BigDecimal totalOutstanding = supplierOutstanding.stream()
                .map(SupplierOutstandingDTO::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("supplierOutstanding", supplierOutstanding);
        model.addAttribute("totalOutstanding", totalOutstanding);

        return "accounting/AP/report-supplier";
    }

    // ─────────────────────────────────────────────
    // POST : Update AP Status
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/status")
    public String updateAPStatus(
            @PathVariable Long id,
            @RequestParam APStatus newStatus,
            RedirectAttributes ra
    ) {
        try {
            apService.updateAPStatus(id, newStatus);
            ra.addFlashAttribute("success", "Status updated successfully!");
            return "redirect:/accounting/payable/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update status: " + e.getMessage());
            return "redirect:/accounting/payable/" + id;
        }
    }

    // ─────────────────────────────────────────────
    // HELPER : Get Current Business
    // ─────────────────────────────────────────────
    private Business getCurrentBusiness(UserDetails userDetails) {
        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getBusiness();
    }
}