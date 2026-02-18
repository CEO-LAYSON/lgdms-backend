package com.crn.lgdms.modules.payments.domain.entity;

import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.users.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cashbook_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashbookEntry extends BaseEntity {

    @Column(name = "entry_number", nullable = false, unique = true, length = 50)
    private String entryNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "entry_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    @Column(name = "payment_method", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private com.crn.lgdms.common.enums.PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // PAYMENT, SALE, etc.

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public enum EntryType {
        RECEIPT,    // Money in
        PAYMENT     // Money out
    }
}
