package com.crn.lgdms.modules.sales.dto.response;

import com.crn.lgdms.modules.sales.domain.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private String id;
    private String customerNumber;
    private String name;
    private String phone;
    private String email;
    private String address;
    private Customer.CustomerType customerType;
    private String taxId;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;
    private boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
