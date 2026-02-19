package com.crn.lgdms.modules.credit.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgingBucket {
    private String accountId;
    private String customerName;
    private String locationName;
    private BigDecimal current;
    private BigDecimal days1to30;
    private BigDecimal days31to60;
    private BigDecimal days61to90;
    private BigDecimal over90;
    private BigDecimal total;

    // Helper to calculate bucket based on invoice date
    public static AgingBucket createFromTransactions(String accountId, String customerName,
                                                     String locationName,
                                                     java.util.List<CreditTransaction> transactions,
                                                     LocalDate asOfDate) {
        AgingBucket bucket = AgingBucket.builder()
            .accountId(accountId)
            .customerName(customerName)
            .locationName(locationName)
            .current(BigDecimal.ZERO)
            .days1to30(BigDecimal.ZERO)
            .days31to60(BigDecimal.ZERO)
            .days61to90(BigDecimal.ZERO)
            .over90(BigDecimal.ZERO)
            .total(BigDecimal.ZERO)
            .build();

        for (CreditTransaction tx : transactions) {
            if (tx.getTransactionType() == CreditTransaction.TransactionType.SALE) {
                long daysOverdue = ChronoUnit.DAYS.between(
                    tx.getTransactionDate().toLocalDate(), asOfDate);

                if (daysOverdue <= 0) {
                    bucket.setCurrent(bucket.getCurrent().add(tx.getAmount()));
                } else if (daysOverdue <= 30) {
                    bucket.setDays1to30(bucket.getDays1to30().add(tx.getAmount()));
                } else if (daysOverdue <= 60) {
                    bucket.setDays31to60(bucket.getDays31to60().add(tx.getAmount()));
                } else if (daysOverdue <= 90) {
                    bucket.setDays61to90(bucket.getDays61to90().add(tx.getAmount()));
                } else {
                    bucket.setOver90(bucket.getOver90().add(tx.getAmount()));
                }

                bucket.setTotal(bucket.getTotal().add(tx.getAmount()));
            } else if (tx.getTransactionType() == CreditTransaction.TransactionType.PAYMENT) {
                // Payments reduce the total - in real implementation, you'd need to allocate
                // payments to specific invoices (FIFO, specific invoice, etc.)
                bucket.setTotal(bucket.getTotal().subtract(tx.getAmount()));
            }
        }

        return bucket;
    }
}
