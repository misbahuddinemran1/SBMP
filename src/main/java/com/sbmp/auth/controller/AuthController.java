package com.sbmp.auth.controller;

import com.sbmp.auth.dto.request.RegisterRequest;
import com.sbmp.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/register")
    public String registerPage() {
        return "user/registration";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute RegisterRequest request
    ) {

        authService.register(request);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }
}
