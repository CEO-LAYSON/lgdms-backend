package com.crn.lgdms.modules.sales.dto.response;

import com.crn.lgdms.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private String id;
    private String invoiceNumber;
    private String locationId;
    private String locationName;
    private String customerId;
    private String customerName;
    private String salesPersonId;
    private String salesPersonName;
    private LocalDate saleDate;
    private LocalDateTime saleTime;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;
    private String paymentMethods;
    private boolean isCreditSale;
    private TransactionStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SaleItemResponse> items;
}
