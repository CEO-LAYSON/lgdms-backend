package com.crn.lgdms.modules.payments.dto.response;

import com.crn.lgdms.common.enums.PaymentMethod;
import com.crn.lgdms.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String paymentNumber;
    private String locationId;
    private String locationName;
    private String customerId;
    private String customerName;
    private String receivedById;
    private String receivedByName;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String referenceNumber;
    private String bankName;
    private LocalDateTime chequeDate;
    private TransactionStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AllocationResponse> allocations;
    private BigDecimal allocatedAmount;
    private BigDecimal unallocatedAmount;
}
