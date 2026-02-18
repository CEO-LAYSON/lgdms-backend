package com.crn.lgdms.modules.receiving.dto.request;

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
public class UpdateReceivingRequest {

    private LocalDate receivingDate;

    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    private String invoiceNumber;

    @Size(max = 50, message = "Delivery note number must not exceed 50 characters")
    private String deliveryNoteNumber;

    @Size(max = 100, message = "Received by must not exceed 100 characters")
    private String receivedBy;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
