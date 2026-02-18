package com.crn.lgdms.modules.transfer.dto.response;

import com.crn.lgdms.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String id;
    private String transferNumber;
    private String transferRequestId;
    private String transferRequestNumber;
    private String fromLocationId;
    private String fromLocationName;
    private String toLocationId;
    private String toLocationName;
    private LocalDate transferDate;
    private String dispatchedBy;
    private LocalDateTime dispatchedAt;
    private String receivedBy;
    private LocalDateTime receivedAt;
    private TransactionStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TransferItemResponse> items;
}
