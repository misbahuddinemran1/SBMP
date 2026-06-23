package com.sbmp.accounting.payable.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ap_payment_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class APPaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ap_id", nullable = false)
    private AccountsPayable accountsPayable;

    /**
     * Scheduled payment due date
     */
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    /**
     * Payment amount for this schedule
     */
    @Column(name = "scheduled_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal scheduledAmount;

    /**
     * Amount actually paid for this schedule
     */
    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * Status: PENDING, PARTIALLY_PAID, PAID, OVERDUE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentScheduleStatus status = PaymentScheduleStatus.PENDING;

    /**
     * Payment reference/transaction ID
     */
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    /**
     * Notes
     */
    @Column(name = "notes", length = 255)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isOverdue() {
        return LocalDate.now().isAfter(scheduledDate) && status != PaymentScheduleStatus.PAID;
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(scheduledDate, LocalDate.now());
    }

    public enum PaymentScheduleStatus {
        PENDING, PARTIALLY_PAID, PAID, OVERDUE
    }
}