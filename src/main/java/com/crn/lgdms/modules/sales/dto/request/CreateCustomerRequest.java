package com.crn.lgdms.modules.sales.dto.request;

import com.crn.lgdms.modules.sales.domain.entity.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "^(\\+255|0)[67]\\d{8}$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    private Customer.CustomerType customerType = Customer.CustomerType.RETAIL;

    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    private BigDecimal creditLimit;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
