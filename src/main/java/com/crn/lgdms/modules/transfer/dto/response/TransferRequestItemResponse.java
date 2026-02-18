package com.crn.lgdms.modules.transfer.dto.response;

import com.crn.lgdms.common.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestItemResponse {
    private String id;
    private String cylinderSizeId;
    private String cylinderSizeName;
    private ProductType productType;
    private Integer requestedQuantity;
    private Integer approvedQuantity;
    private String notes;
}
