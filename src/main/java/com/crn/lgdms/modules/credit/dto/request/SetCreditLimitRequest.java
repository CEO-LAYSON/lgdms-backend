package com.crn.lgdms.modules.credit.dto.request;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetCreditLimitRequest {

    private String customerId; // Either customer OR location
    private String locationId;  // For vehicle/branch credit

    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "0", message = "Limit must be greater than or equal to 0")
    private BigDecimal limitAmount;

    private LocalDateTime effectiveFrom = LocalDateTime.now();

    private LocalDateTime effectiveTo;

    @NotBlank(message = "Approved by is required")
    private String approvedBy;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;
}
