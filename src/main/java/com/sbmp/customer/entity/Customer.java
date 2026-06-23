package com.sbmp.customer.entity;

import com.sbmp.business.entity.Business;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "customers",
        indexes = {

                @Index(name = "idx_customer_business", columnList = "business_id"),
                @Index(name = "idx_customer_mobile", columnList = "mobile"),
                @Index(name = "idx_customer_code", columnList = "customer_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    // ─────────────────────────────
    // PRIMARY KEY
    // ─────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─────────────────────────────
    // CUSTOMER INFO
    // ─────────────────────────────
    @Column(name = "customer_code", nullable = false, unique = true, length = 30)
    private String customerCode;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String mobile;

    @Size(max = 150)
    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String customerType;

    @Column(length = 50)
    private String source;

    // ─────────────────────────────
    // ACCOUNT (ERP CORE)
    // ─────────────────────────────

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dueAmount = BigDecimal.ZERO;

    // ─────────────────────────────
    // STATUS
    // ─────────────────────────────
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // ─────────────────────────────
    // RELATION
    // ─────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    // ─────────────────────────────
    // AUDIT
    // ─────────────────────────────
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}