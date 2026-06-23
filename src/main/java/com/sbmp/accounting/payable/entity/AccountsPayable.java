package com.sbmp.accounting.payable.entity;

import com.sbmp.inventory.purchase.entity.Purchase;
import com.sbmp.inventory.supplier.entity.Supplier;
import com.sbmp.business.entity.Business;
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
@Table(name = "accounts_payable")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsPayable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false, unique = true)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    /**
     * Invoice number from supplier
     */
    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Original invoice amount
     */
    @Column(name = "invoice_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal invoiceAmount;

    /**
     * Amount already paid
     */
    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * Outstanding/Remaining balance
     */
    @Column(name = "outstanding_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingAmount;

    /**
     * Status: PENDING, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private APStatus status = APStatus.PENDING;

    /**
     * Payment terms in days (e.g., 30 for Net 30)
     */
    @Column(name = "payment_terms_days", nullable = false)
    @Builder.Default
    private Integer paymentTermsDays = 0;

    /**
     * Discount available if paid early (percentage)
     */
    @Column(name = "early_payment_discount", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal earlyPaymentDiscount = BigDecimal.ZERO;

    /**
     * Notes or special instructions
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "accountsPayable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<APPaymentSchedule> paymentSchedules = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addPaymentSchedule(APPaymentSchedule schedule) {
        paymentSchedules.add(schedule);
        schedule.setAccountsPayable(this);
    }

    public void removePaymentSchedule(APPaymentSchedule schedule) {
        paymentSchedules.remove(schedule);
        schedule.setAccountsPayable(null);
    }

    /**
     * Check if this AP is overdue
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && status != APStatus.PAID;
    }

    /**
     * Get number of days overdue (negative if not overdue)
     */
    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public enum APStatus {
        PENDING, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED
    }
}