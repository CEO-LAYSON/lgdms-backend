package com.crn.lgdms.modules.credit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditAgingResponse {
    private List<AgingBucketResponse> buckets;
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgingBucketResponse {
        private String accountId;
        private String customerName;
        private String locationName;
        private BigDecimal current;
        private BigDecimal days1to30;
        private BigDecimal days31to60;
        private BigDecimal days61to90;
        private BigDecimal over90;
        private BigDecimal total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private BigDecimal totalCurrent;
        private BigDecimal total1to30;
        private BigDecimal total31to60;
        private BigDecimal total61to90;
        private BigDecimal totalOver90;
        private BigDecimal grandTotal;
    }
}
