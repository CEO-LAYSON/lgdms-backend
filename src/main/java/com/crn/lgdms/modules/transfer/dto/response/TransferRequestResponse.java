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
public class TransferRequestResponse {
    private String id;
    private String requestNumber;
    private String fromLocationId;
    private String fromLocationName;
    private String toLocationId;
    private String toLocationName;
    private LocalDate requestDate;
    private String requestedBy;
    private LocalDate expectedDeliveryDate;
    private TransactionStatus status;
    private String notes;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TransferRequestItemResponse> items;
}
