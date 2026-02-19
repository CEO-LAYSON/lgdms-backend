package com.crn.lgdms.modules.inventory.dto.request;

import com.crn.lgdms.common.enums.ProductType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconcileRequest {

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotNull(message = "Reconciliation date is required")
    private LocalDate reconciliationDate;

    @NotEmpty(message = "At least one entry is required")
    private List<ReconcileEntry> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReconcileEntry {
        @NotBlank(message = "Cylinder size ID is required")
        private String cylinderSizeId;

        @NotNull(message = "Product type is required")
        private ProductType productType;

        @NotNull(message = "Physical count is required")
        @Min(value = 0, message = "Count cannot be negative")
        private Integer physicalCount;

        private String notes;
    }
}
