package com.crn.lgdms.modules.receiving.domain.entity;

import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.masterdata.domain.entity.Supplier;
import com.crn.lgdms.modules.users.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods_receiving")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiving extends BaseEntity {

    @Column(name = "receiving_number", nullable = false, unique = true, length = 50)
    private String receivingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location; // Usually HQ

    @Column(name = "receiving_date", nullable = false)
    private LocalDate receivingDate;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "delivery_note_number", length = 50)
    private String deliveryNoteNumber;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "received_by", length = 100)
    private String receivedBy;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "goodsReceiving", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReceivingItem> items = new ArrayList<>();
}
