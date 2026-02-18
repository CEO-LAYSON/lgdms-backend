package com.crn.lgdms.modules.transfer.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransferRequestRequest {

    @NotBlank(message = "From location ID is required")
    private String fromLocationId;

    @NotBlank(message = "To location ID is required")
    private String toLocationId;

    @NotNull(message = "Request date is required")
    private LocalDate requestDate;

    @NotBlank(message = "Requested by is required")
    @Size(max = 100, message = "Requested by must not exceed 100 characters")
    private String requestedBy;

    private LocalDate expectedDeliveryDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Valid
    @NotEmpty(message = "At least one item is required")
    private List<AddTransferRequestItemRequest> items = new ArrayList<>();
}
