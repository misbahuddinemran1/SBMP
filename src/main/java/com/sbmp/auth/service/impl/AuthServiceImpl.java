package com.sbmp.auth.service.impl;

import com.sbmp.auth.dto.request.RegisterRequest;
import com.sbmp.auth.service.AuthService;
import com.sbmp.business.entity.Business;
import com.sbmp.business.repository.BusinessRepository;
import com.sbmp.common.enums.Role;
import com.sbmp.user.entity.User;
import com.sbmp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Business business = Business.builder()
                .businessName(request.getBusinessName())
                .businessType(request.getBusinessType())
                .address(request.getAddress())
                .build();

        Business savedBusiness = businessRepository.save(business);

        User user = User.builder()
                .business(savedBusiness)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.BUSINESS_OWNER)
                .build();

        userRepository.save(user);
    }
}