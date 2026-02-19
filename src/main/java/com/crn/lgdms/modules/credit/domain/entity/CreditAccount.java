package com.crn.lgdms.modules.credit.domain.entity;

import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.sales.domain.entity.Customer;
import com.crn.lgdms.modules.users.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "credit_accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditAccount extends BaseEntity {

    @Column(name = "account_number", nullable = false, unique = true, length = 50)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer; // For customer credit

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location; // For vehicle/internal credit (Vehicles as customers!)

    @Column(name = "account_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CreditAccountType accountType;

    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "available_credit", precision = 19, scale = 2)
    private BigDecimal availableCredit;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms; // e.g., "NET30", "NET15"

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate; // For overdue accounts

    @Column(name = "last_statement_date")
    private LocalDateTime lastStatementDate;

    @Column(name = "next_due_date")
    private LocalDateTime nextDueDate;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "creditAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CreditTransaction> transactions = new ArrayList<>();

    public enum CreditAccountType {
        CUSTOMER,      // Regular customer credit
        VEHICLE,       // Vehicle as internal customer (from SRS!)
        BRANCH         // Branch credit (if needed)
    }

    @PrePersist
    @PreUpdate
    public void calculateAvailableCredit() {
        this.availableCredit = creditLimit.subtract(currentBalance);
    }
}
