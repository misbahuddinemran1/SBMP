package com.sbmp.customer.entity;

import com.sbmp.business.entity.Business;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "customers",
        indexes = {

                @Index(
                        name = "idx_customer_business",
                        columnList = "business_id"
                ),

                @Index(
                        name = "idx_customer_mobile",
                        columnList = "mobile"
                ),

                @Index(
                        name = "idx_customer_code",
                        columnList = "customer_code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    // ─────────────────────────────────────────────
    // PRIMARY KEY
    // ─────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─────────────────────────────────────────────
    // CUSTOMER INFORMATION
    // ─────────────────────────────────────────────

    @Column(
            name = "customer_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String customerCode;

    @NotBlank(message = "Customer name is required")
    @Size(max = 150)
    @Column(
            nullable = false,
            length = 150
    )
    private String name;

    @NotBlank(message = "Mobile number is required")
    @Size(max = 20)
    @Column(
            nullable = false,
            length = 20
    )
    private String mobile;

    @Size(max = 150)
    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    // RETAIL / WHOLESALE / DEALER / CORPORATE
    @Column(length = 50)
    private String customerType;

    // WALK_IN / FACEBOOK / WEBSITE / WHATSAPP / REFERRAL
    @Column(length = 50)
    private String source;

    // ─────────────────────────────────────────────
    // STATUS
    // ─────────────────────────────────────────────

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // ─────────────────────────────────────────────
    // RELATIONSHIP
    // ─────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "business_id",
            nullable = false
    )
    private Business business;

    // ─────────────────────────────────────────────
    // AUDIT
    // ─────────────────────────────────────────────

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────

    public boolean isActive() {

        return Boolean.TRUE.equals(active);
    }
}