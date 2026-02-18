package com.crn.lgdms.modules.receiving.dto.response;

import com.crn.lgdms.common.enums.ProductType;
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
public class ReceivingItemResponse {
    private String id;
    private String cylinderSizeId;
    private String cylinderSizeName;
    private ProductType productType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private String notes;
    private String stockLedgerId;
}
