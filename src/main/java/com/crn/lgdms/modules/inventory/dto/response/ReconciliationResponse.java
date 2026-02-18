package com.crn.lgdms.modules.inventory.dto.response;

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
public class ReconciliationResponse {
    private String id;
    private String locationId;
    private String locationName;
    private LocalDate reconciliationDate;
    private String reconciledBy;
    private LocalDateTime reconciledAt;
    private List<ReconciliationEntryResponse> entries;
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReconciliationEntryResponse {
        private String cylinderSizeId;
        private String cylinderSizeName;
        private ProductType productType;
        private Integer systemQuantity;
        private Integer physicalQuantity;
        private Integer variance;
        private String status; // MATCH, MISMATCH
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer totalItems;
        private Integer matchingItems;
        private Integer mismatchingItems;
        private Integer totalVariance;
        private String reconciliationStatus; // PASSED, FAILED
    }
}
