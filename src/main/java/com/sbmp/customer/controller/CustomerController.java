package com.sbmp.customer.controller;

import com.sbmp.business.entity.Business;
import com.sbmp.customer.dto.CustomerCreateDto;
import com.sbmp.customer.dto.CustomerUpdateDto;
import com.sbmp.customer.entity.Customer;
import com.sbmp.customer.exception.CustomerNotFoundException;
import com.sbmp.customer.service.CustomerService;
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
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final UserRepository userRepository;

    private static final String LIST_VIEW =
            "customer/list";

    private static final String ADD_VIEW =
            "customer/add";

    private static final String EDIT_VIEW =
            "customer/edit";

    private static final String DETAILS_VIEW =
            "customer/details";

    private static final String REDIRECT_LIST =
            "redirect:/customers";

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
                        .orElseThrow();

        return user.getBusiness();
    }

    // ─────────────────────────────────────────────
    // COMMON ATTRIBUTES
    // ─────────────────────────────────────────────

    private void addCommonAttributes(
            Model model,
            Business business
    ) {

        model.addAttribute(
                "activePage",
                "customers"
        );

        model.addAttribute(
                "businessName",
                business.getBusinessName()
        );

        model.addAttribute(
                "totalCustomers",
                customerService.countTotal(
                        business
                )
        );

        model.addAttribute(
                "activeCustomers",
                customerService.countActive(
                        business
                )
        );

        model.addAttribute(
                "inactiveCustomers",
                customerService.countInactive(
                        business
                )
        );
    }

    // ─────────────────────────────────────────────
    // CUSTOMER LIST
    // ─────────────────────────────────────────────

    @GetMapping
    public String list(

            @AuthenticationPrincipal
            UserDetails userDetails,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            String customerType,

            @RequestParam(required = false)
            String source,

            @RequestParam(required = false)
            Boolean active,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            Model model
    ) {

        Business business =
                getCurrentBusiness(
                        userDetails
                );

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<Customer> customers =
                customerService.search(

                        business,

                        keyword,

                        customerType,

                        source,

                        active,

                        pageable
                );

        addCommonAttributes(
                model,
                business
        );

        model.addAttribute(
                "customers",
                customers
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "customerType",
                customerType
        );

        model.addAttribute(
                "source",
                source
        );

        model.addAttribute(
                "active",
                active
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
                getCurrentBusiness(
                        userDetails
                );

        addCommonAttributes(
                model,
                business
        );

        model.addAttribute(
                "customer",
                new CustomerCreateDto()
        );

        return ADD_VIEW;
    }

    // ─────────────────────────────────────────────
    // SAVE CUSTOMER
    // ─────────────────────────────────────────────

    @PostMapping("/save")
    public String save(

            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @ModelAttribute("customer")
            CustomerCreateDto dto,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        Business business =
                getCurrentBusiness(
                        userDetails
                );

        if (bindingResult.hasErrors()) {

            addCommonAttributes(
                    model,
                    business
            );

            return ADD_VIEW;
        }

        customerService.save(
                business,
                dto
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Customer created successfully."
        );

        return REDIRECT_LIST;
    }

    // ─────────────────────────────────────────────
    // DETAILS
    // ─────────────────────────────────────────────

    @GetMapping("/{id}")
    public String details(

            @PathVariable
            Long id,

            @AuthenticationPrincipal
            UserDetails userDetails,

            Model model
    ) {

        Business business =
                getCurrentBusiness(
                        userDetails
                );

        Customer customer =
                customerService
                        .getByIdAndBusiness(
                                id,
                                business
                        );

        addCommonAttributes(
                model,
                business
        );

        model.addAttribute(
                "customer",
                customer
        );

        return DETAILS_VIEW;
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
                getCurrentBusiness(
                        userDetails
                );

        Customer customer =
                customerService
                        .getByIdAndBusiness(
                                id,
                                business
                        );

        CustomerUpdateDto dto =
                new CustomerUpdateDto();

        dto.setName(
                customer.getName()
        );

        dto.setMobile(
                customer.getMobile()
        );

        dto.setEmail(
                customer.getEmail()
        );

        dto.setAddress(
                customer.getAddress()
        );

        dto.setCustomerType(
                customer.getCustomerType()
        );

        dto.setSource(
                customer.getSource()
        );

        dto.setActive(
                customer.getActive()
        );
        dto.setCustomerCode(customer.getCustomerCode());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());

        addCommonAttributes(
                model,
                business
        );

        model.addAttribute(
                "customer",
                dto
        );

        model.addAttribute(
                "customerId",
                id
        );

        return EDIT_VIEW;
    }

    // ─────────────────────────────────────────────
    // UPDATE CUSTOMER
    // ─────────────────────────────────────────────

    @PostMapping("/{id}/update")
    public String update(

            @PathVariable
            Long id,

            @AuthenticationPrincipal
            UserDetails userDetails,

            @Valid
            @ModelAttribute("customer")
            CustomerUpdateDto dto,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        Business business =
                getCurrentBusiness(
                        userDetails
                );

        if (bindingResult.hasErrors()) {

            addCommonAttributes(
                    model,
                    business
            );

            model.addAttribute(
                    "customerId",
                    id
            );

            return EDIT_VIEW;
        }

        customerService.update(
                id,
                business,
                dto
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Customer updated successfully."
        );

        return REDIRECT_LIST;
    }

    // ─────────────────────────────────────────────
    // DELETE CUSTOMER
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
                getCurrentBusiness(
                        userDetails
                );

        try {

            customerService.delete(
                    id,
                    business
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Customer deleted successfully."
            );

        } catch (
                CustomerNotFoundException ex
        ) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return REDIRECT_LIST;
    }
}