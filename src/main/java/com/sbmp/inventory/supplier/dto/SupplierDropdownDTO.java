package com.sbmp.inventory.supplier.dto;

import com.sbmp.inventory.supplier.entity.Supplier;
import lombok.*;

/**
 * Lightweight DTO used only for populating the Supplier
 * dropdown on the New Purchase Invoice page.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDropdownDTO {

    private Long   id;
    private String name;       // ← supplierName
    private String company;    // ← companyName
    private String phone;
    private String address;

    /** Static factory — maps from Supplier entity */
    public static SupplierDropdownDTO from(Supplier s) {
        return SupplierDropdownDTO.builder()
                .id     (s.getId())
                .name   (s.getSupplierName())
                .company(s.getCompanyName() != null ? s.getCompanyName() : "—")
                .phone  (s.getPhone()       != null ? s.getPhone()       : "—")
                .address(s.getAddress()     != null ? s.getAddress()     : "—")
                .build();
    }
}
