package com.sbmp.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String businessName;

    private String businessType;

    private String address;

    private String name;

    private String phone;

    private String email;

    private String password;
}