package com.crn.lgdms.modules.inventory.domain.entity;

import com.crn.lgdms.common.enums.MovementType;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.users.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "empty_ledger")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmptyLedger extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cylinder_size_id", nullable = false)
    private CylinderSize cylinderSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // Positive for IN, Negative for OUT

    @Column(name = "running_balance", nullable = false)
    private Integer runningBalance; // Current empty balance

    @Column(name = "reference_type", length = 50)
    private String referenceType; // TRANSFER, SALE, RETURN, etc.

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
    }
}
