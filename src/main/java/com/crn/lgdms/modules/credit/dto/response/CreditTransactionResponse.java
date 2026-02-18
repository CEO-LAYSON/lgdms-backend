package com.crn.lgdms.modules.credit.dto.response;

import com.crn.lgdms.modules.credit.domain.entity.CreditTransaction;
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
public class CreditTransactionResponse {
    private String id;
    private String transactionNumber;
    private String accountNumber;
    private String customerName;
    private String locationName;
    private CreditTransaction.TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private LocalDateTime transactionDate;
    private String saleInvoice;
    private String paymentNumber;
    private String status;
}
