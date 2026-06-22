package com.sbmp.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerUpdateDto {

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

    private Boolean active;
    private String customerCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}