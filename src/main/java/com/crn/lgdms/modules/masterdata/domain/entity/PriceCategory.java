package com.crn.lgdms.modules.masterdata.domain.entity;

import com.crn.lgdms.modules.users.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "price_categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCategory extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g., "Retail", "Wholesale", "Special"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cylinder_size_id", nullable = false)
    private CylinderSize cylinderSize;

    @Column(name = "product_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private com.crn.lgdms.common.enums.ProductType productType;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "min_quantity")
    private Integer minQuantity;

    @Column(name = "max_quantity")
    private Integer maxQuantity;

    @Column(name = "applicable_locations", columnDefinition = "TEXT")
    private String applicableLocations; // JSON or comma-separated location types/IDs
}
