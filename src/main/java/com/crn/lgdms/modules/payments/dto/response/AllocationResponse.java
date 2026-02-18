package com.crn.lgdms.modules.payments.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationResponse {
    private String id;
    private String paymentId;
    private String paymentNumber;
    private String saleId;
    private String saleInvoice;
    private BigDecimal allocatedAmount;
    private LocalDateTime allocationDate;
    private String notes;
}
