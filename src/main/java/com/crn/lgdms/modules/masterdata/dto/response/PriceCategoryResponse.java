package com.crn.lgdms.modules.masterdata.dto.response;

import com.crn.lgdms.common.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCategoryResponse {
    private String id;
    private String name;
    private String cylinderSizeId;
    private String cylinderSizeName;
    private ProductType productType;
    private BigDecimal price;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean isActive;
    private Integer minQuantity;
    private Integer maxQuantity;
    private String applicableLocations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
