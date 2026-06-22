package com.sbmp.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerCreateDto {

    @NotBlank(message = "Customer name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Mobile number is required")
    @Size(max = 20)
    private String mobile;

    @Size(max = 150)
    private String email;

    private String address;

    private String customerType;

    private String source;

    private Boolean active = true;
}