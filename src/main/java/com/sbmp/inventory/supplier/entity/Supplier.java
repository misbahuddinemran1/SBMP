package com.sbmp.inventory.supplier.entity;

import com.sbmp.business.entity.Business;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─────────────────────────────────────────────────────────────
    // BUSINESS
    // ─────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id",
            nullable = false)
    private Business business;

    // ─────────────────────────────────────────────────────────────
    // BASIC INFORMATION
    // ─────────────────────────────────────────────────────────────

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150)
    @Column(nullable = false,
            length = 150)
    private String supplierName;

    @Size(max = 150)
    @Column(length = 150)
    private String companyName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20)
    @Column(nullable = false,
            length = 20)
    private String phone;

    @Email(message = "Invalid email address")
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    @Size(max = 500)
    @Column(length = 500)
    private String address;

    // ─────────────────────────────────────────────────────────────
    // STATUS
    // ─────────────────────────────────────────────────────────────

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // ─────────────────────────────────────────────────────────────
    // AUDIT
    // ─────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false,
            updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}