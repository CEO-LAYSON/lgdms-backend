package com.crn.lgdms.modules.transfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveTransferRequest {

    @NotBlank(message = "Received by is required")
    private String receivedBy;

    private List<ReceivedItem> receivedItems;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceivedItem {
        private String transferItemId;
        private Integer receivedQuantity;
        private Integer emptyReturnedQuantity; // For refill rule
    }
}
