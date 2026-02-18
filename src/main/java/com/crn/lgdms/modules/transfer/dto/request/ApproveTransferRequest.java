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
public class ApproveTransferRequest {

    @NotBlank(message = "Reviewed by is required")
    private String reviewedBy;

    private List<ApprovedItem> approvedItems;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovedItem {
        private String transferRequestItemId;
        private Integer approvedQuantity;
    }
}
