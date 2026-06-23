package com.sbmp.sales.entity;

import com.sbmp.business.entity.Business;
import com.sbmp.customer.entity.Customer;
import com.sbmp.sales.enums.PaymentStatus;
import com.sbmp.sales.enums.SaleStatus;
import com.sbmp.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "sales",
        indexes = {

                @Index(
                        name = "idx_sale_invoice",
                        columnList = "invoice_no"
                ),

                @Index(
                        name = "idx_sale_business",
                        columnList = "business_id"
                ),

                @Index(
                        name = "idx_sale_customer",
                        columnList = "customer_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "invoice_no",
            nullable = false,
            unique = true,
            length = 30
    )
    private String invoiceNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "business_id",
            nullable = false
    )
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by"
    )
    private User createdBy;

    @Column(
            name = "sale_date",
            nullable = false
    )
    private LocalDate saleDate;

    // ------------------------------
    // Amounts
    // ------------------------------

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    @Builder.Default
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(
            name = "invoice_discount",
            nullable = false,
            precision = 10,
            scale = 2
    )
    @Builder.Default
    private BigDecimal invoiceDiscount = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    @Builder.Default
    private BigDecimal dueAmount = BigDecimal.ZERO;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    @Builder.Default
    private BigDecimal advancePaid = BigDecimal.ZERO;

    // ------------------------------
    // Status
    // ------------------------------

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private SaleStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ------------------------------
    // Items
    // ------------------------------

    @OneToMany(
            mappedBy = "sale",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<SaleItem> items =
            new ArrayList<>();

    // ------------------------------
    // Payments
    // ------------------------------

    @OneToMany(
            mappedBy = "sale",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<SalePayment> payments =
            new ArrayList<>();

    // ------------------------------
    // Audit
    // ------------------------------

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

    // ------------------------------
    // Helpers
    // ------------------------------

    public void addItem(
            SaleItem item
    ) {

        items.add(item);

        item.setSale(this);
    }

    public void removeItem(
            SaleItem item
    ) {

        items.remove(item);

        item.setSale(null);
    }

    public void addPayment(
            SalePayment payment
    ) {

        payments.add(payment);

        payment.setSale(this);
    }

    public void removePayment(
            SalePayment payment
    ) {

        payments.remove(payment);

        payment.setSale(null);
    }
}