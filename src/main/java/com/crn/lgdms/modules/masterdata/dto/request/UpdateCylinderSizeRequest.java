package com.crn.lgdms.modules.masterdata.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCylinderSizeRequest {

    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @DecimalMin(value = "0.1", message = "Weight must be greater than 0")
    private BigDecimal weightKg;

    private BigDecimal tareWeightKg;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Boolean isActive;

    private Integer displayOrder;
}
