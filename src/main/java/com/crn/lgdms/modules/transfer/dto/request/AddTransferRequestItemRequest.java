package com.crn.lgdms.modules.transfer.dto.request;

import com.crn.lgdms.common.enums.ProductType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTransferRequestItemRequest {

    @NotBlank(message = "Cylinder size ID is required")
    private String cylinderSizeId;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotNull(message = "Requested quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer requestedQuantity;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}
