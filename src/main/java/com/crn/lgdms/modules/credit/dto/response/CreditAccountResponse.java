package com.crn.lgdms.modules.credit.dto.response;

import com.crn.lgdms.modules.credit.domain.entity.CreditAccount;
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
public class CreditAccountResponse {
    private String id;
    private String accountNumber;
    private String customerId;
    private String customerName;
    private String locationId;
    private String locationName;
    private CreditAccount.CreditAccountType accountType;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;
    private BigDecimal availableCredit;
    private String paymentTerms;
    private BigDecimal interestRate;
    private LocalDateTime lastStatementDate;
    private LocalDateTime nextDueDate;
    private boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
