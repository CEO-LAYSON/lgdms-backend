package com.crn.lgdms.modules.receiving.domain.entity;

import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.modules.inventory.domain.entity.StockLedger;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.users.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "receiving_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_id", nullable = false)
    private GoodsReceiving goodsReceiving;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cylinder_size_id", nullable = false)
    private CylinderSize cylinderSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "manufacture_date")
    private java.time.LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    @Column(name = "notes", length = 255)
    private String notes;

    @OneToOne(mappedBy = "receivingItem", cascade = CascadeType.ALL)
    private StockLedger stockLedger;
}
