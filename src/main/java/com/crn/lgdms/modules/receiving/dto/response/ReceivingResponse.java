package com.crn.lgdms.modules.receiving.dto.response;

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
public class ReceivingResponse {
    private String id;
    private String receivingNumber;
    private String supplierId;
    private String supplierName;
    private String locationId;
    private String locationName;
    private LocalDate receivingDate;
    private String invoiceNumber;
    private String deliveryNoteNumber;
    private BigDecimal totalAmount;
    private Integer totalQuantity;
    private TransactionStatus status;
    private String receivedBy;
    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReceivingItemResponse> items;
}
