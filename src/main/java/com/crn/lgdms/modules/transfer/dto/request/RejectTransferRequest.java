package com.crn.lgdms.modules.transfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectTransferRequest {

    @NotBlank(message = "Reviewed by is required")
    private String reviewedBy;

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 255, message = "Rejection reason must not exceed 255 characters")
    private String rejectionReason;
}
