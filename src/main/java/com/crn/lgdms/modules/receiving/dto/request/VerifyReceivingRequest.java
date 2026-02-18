package com.crn.lgdms.modules.receiving.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyReceivingRequest {

    @NotBlank(message = "Verified by is required")
    private String verifiedBy;

    private String notes;
}
