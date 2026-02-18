package com.crn.lgdms.modules.transfer.domain.entity;

import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.users.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer extends BaseEntity {

    @Column(name = "transfer_number", nullable = false, unique = true, length = 50)
    private String transferNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_request_id")
    private TransferRequest transferRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id", nullable = false)
    private Location fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id", nullable = false)
    private Location toLocation;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(name = "dispatched_by", length = 100)
    private String dispatchedBy;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "received_by", length = 100)
    private String receivedBy;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransferItem> items = new ArrayList<>();
}
