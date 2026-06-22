package com.sbmp.inventory.supplier.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSupplierRequest {

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    private String companyName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20)
    private String phone;

    @Email(message = "Invalid email")
    private String email;

    private String address;

    private Boolean active = true;
}