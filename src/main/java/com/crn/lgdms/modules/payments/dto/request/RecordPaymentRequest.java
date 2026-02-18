package com.crn.lgdms.modules.payments.dto.request;

import com.crn.lgdms.common.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class RecordPaymentRequest {

    @NotBlank(message = "Location ID is required")
    private String locationId;

    private String customerId; // Optional - for customer payments

    @NotBlank(message = "Received by is required")
    private String receivedBy;

    @NotNull(message = "Payment date is required")
    private LocalDateTime paymentDate;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    private LocalDateTime chequeDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private List<AllocationRequest> allocations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationRequest {
        @NotBlank(message = "Sale ID is required")
        private String saleId;

        @NotNull(message = "Allocated amount is required")
        @DecimalMin(value = "0.01", message = "Allocated amount must be greater than 0")
        private BigDecimal allocatedAmount;
    }
}
