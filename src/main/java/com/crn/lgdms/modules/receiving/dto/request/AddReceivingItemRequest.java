package com.crn.lgdms.modules.receiving.dto.request;

import com.crn.lgdms.common.enums.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AddReceivingItemRequest {

    @NotBlank(message = "Cylinder size ID is required")
    private String cylinderSizeId;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0", message = "Unit price must be greater than or equal to 0")
    private BigDecimal unitPrice;

    @Size(max = 50, message = "Batch number must not exceed 50 characters")
    private String batchNumber;

    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}
