package com.crn.lgdms.modules.transfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransferRequest {

    @NotBlank(message = "Transfer request ID is required")
    private String transferRequestId;

    @NotNull(message = "Transfer date is required")
    private LocalDate transferDate;

    @NotBlank(message = "Dispatched by is required")
    @Size(max = 100, message = "Dispatched by must not exceed 100 characters")
    private String dispatchedBy;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
