package com.crn.lgdms.modules.sales.domain.entity;

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
import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cylinder_size_id", nullable = false)
    private CylinderSize cylinderSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount", precision = 19, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "empty_returned")
    private Boolean emptyReturned = false; // For refill sales

    @Column(name = "empty_quantity")
    private Integer emptyQuantity; // How many empties returned

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "notes", length = 255)
    private String notes;

    @OneToOne(mappedBy = "saleItem", cascade = CascadeType.ALL)
    private StockLedger stockLedger;

    @OneToOne(mappedBy = "saleItem", cascade = CascadeType.ALL)
    private EmptyLedger emptyLedger;
}
