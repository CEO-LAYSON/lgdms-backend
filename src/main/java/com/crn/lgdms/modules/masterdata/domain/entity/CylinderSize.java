package com.crn.lgdms.modules.masterdata.domain.entity;

import com.crn.lgdms.modules.users.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "cylinder_sizes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CylinderSize extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name; // e.g., "3kg", "6kg", "15kg", "38kg", "100kg"

    @Column(name = "weight_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "tare_weight_kg", precision = 10, scale = 2)
    private BigDecimal tareWeightKg; // Empty cylinder weight

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder;
}
