package com.sbmp.inventory.purchase.entity;
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
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(name = "purchase_number", unique = true, nullable = false, length = 20)
private String purchaseNumber;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "supplier_id", nullable = false)
private Supplier supplier;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "business_id", nullable = false)
private Business business;

@Column(name = "purchase_date", nullable = false)
private LocalDate purchaseDate;

@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
private PurchaseStatus status;

@Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
private BigDecimal subtotal;

@Column(name = "total_discount", nullable = false, precision = 15, scale = 2)
private BigDecimal totalDiscount;

@Column(name = "grand_total", nullable = false, precision = 15, scale = 2)
private BigDecimal grandTotal;

@Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
private BigDecimal paidAmount;

@Column(name = "due_amount", nullable = false, precision = 15, scale = 2)
private BigDecimal dueAmount;

@Column(name = "notes", columnDefinition = "TEXT")
private String notes;

@OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@Builder.Default
private List < PurchaseItem > items = new ArrayList<>();

@OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@Builder.Default
private List < PurchasePayment > payments = new ArrayList<>();

    @Column(name = "advance_paid", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal advancePaid = BigDecimal.ZERO;

@CreationTimestamp
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@UpdateTimestamp
@Column(name = "updated_at")
private LocalDateTime updatedAt;

// Convenience methods
public void addItem(PurchaseItem item) {
items.add(item);
item.setPurchase(this);
}

public void removeItem(PurchaseItem item) {
items.remove(item);
item.setPurchase(null);
}

public void addPayment(PurchasePayment payment) {
payments.add(payment);
payment.setPurchase(this);
}

public enum PurchaseStatus {
DRAFT, COMPLETED, CANCELLED
}
}