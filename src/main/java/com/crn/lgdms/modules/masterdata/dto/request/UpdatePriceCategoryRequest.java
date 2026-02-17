package com.crn.lgdms.modules.masterdata.dto.request;

import com.crn.lgdms.common.enums.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceCategoryRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private String cylinderSizeId;

    private ProductType productType;

    @DecimalMin(value = "0", message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private Boolean isActive;

    private Integer minQuantity;

    private Integer maxQuantity;

    private String applicableLocations;
}
