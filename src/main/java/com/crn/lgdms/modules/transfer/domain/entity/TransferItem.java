package com.crn.lgdms.modules.transfer.domain.entity;

import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.modules.inventory.domain.entity.StockLedger;
import com.crn.lgdms.modules.inventory.domain.entity.EmptyLedger;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.users.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transfer_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cylinder_size_id", nullable = false)
    private CylinderSize cylinderSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "empty_returned_quantity")
    private Integer emptyReturnedQuantity; // For refill rule enforcement

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "notes", length = 255)
    private String notes;

    @OneToOne(mappedBy = "transferItem", cascade = CascadeType.ALL)
    private StockLedger outgoingStockLedger;

    @OneToOne(mappedBy = "transferItem", cascade = CascadeType.ALL)
    private StockLedger incomingStockLedger;

    @OneToOne(mappedBy = "transferItem", cascade = CascadeType.ALL)
    private EmptyLedger emptyLedger;
}
