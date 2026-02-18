package com.crn.lgdms.modules.inventory.dto.request;

import com.crn.lgdms.common.enums.ProductType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdjustmentRequest {

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotBlank(message = "Cylinder size ID is required")
    private String cylinderSizeId;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer newQuantity;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String notes;
}
