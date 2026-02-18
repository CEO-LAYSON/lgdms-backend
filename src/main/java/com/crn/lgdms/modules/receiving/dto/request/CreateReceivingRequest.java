package com.crn.lgdms.modules.receiving.dto.request;

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
public class CreateReceivingRequest {

    @NotBlank(message = "Supplier ID is required")
    private String supplierId;

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotNull(message = "Receiving date is required")
    private LocalDate receivingDate;

    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    private String invoiceNumber;

    @Size(max = 50, message = "Delivery note number must not exceed 50 characters")
    private String deliveryNoteNumber;

    @Size(max = 100, message = "Received by must not exceed 100 characters")
    private String receivedBy;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Valid
    @NotEmpty(message = "At least one item is required")
    private List<AddReceivingItemRequest> items = new ArrayList<>();
}
