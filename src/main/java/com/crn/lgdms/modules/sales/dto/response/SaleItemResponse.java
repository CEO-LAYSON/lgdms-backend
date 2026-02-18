package com.crn.lgdms.modules.sales.dto.response;

import com.crn.lgdms.common.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemResponse {
    private String id;
    private String cylinderSizeId;
    private String cylinderSizeName;
    private ProductType productType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private Boolean emptyReturned;
    private Integer emptyQuantity;
    private String batchNumber;
    private String notes;
    private String stockLedgerId;
    private String emptyLedgerId;
}
