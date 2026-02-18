package com.crn.lgdms.modules.sales.dto.request;

import com.crn.lgdms.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSaleRequest {

    @NotBlank(message = "Location ID is required")
    private String locationId;

    private String customerId; // Optional - can be null for walk-in customers

    @NotBlank(message = "Sales person ID is required")
    private String salesPersonId;

    @NotNull(message = "Sale date is required")
    private LocalDate saleDate;

    @Valid
    @NotEmpty(message = "At least one item is required")
    private List<SaleItemRequest> items = new ArrayList<>();

    private List<SalePaymentRequest> payments;

    private BigDecimal discount = BigDecimal.ZERO;

    private BigDecimal tax = BigDecimal.ZERO;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemRequest {
        @NotBlank(message = "Cylinder size ID is required")
        private String cylinderSizeId;

        @NotNull(message = "Product type is required")
        private com.crn.lgdms.common.enums.ProductType productType;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;

        private BigDecimal discount = BigDecimal.ZERO;

        private Boolean emptyReturned = false; // For refill sales

        @Size(max = 255, message = "Notes must not exceed 255 characters")
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalePaymentRequest {
        @NotNull(message = "Payment method is required")
        private PaymentMethod paymentMethod;

        @NotNull(message = "Amount is required")
        private BigDecimal amount;

        private String referenceNumber;

        private String notes;
    }
}
