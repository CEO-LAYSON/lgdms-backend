package com.crn.lgdms.modules.locations.dto.request;

import com.crn.lgdms.common.enums.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateLocationRequest {

    @NotBlank(message = "Location name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotNull(message = "Location type is required")
    private LocationType locationType;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Pattern(regexp = "^(\\+255|0)[67]\\d{8}$", message = "Invalid phone number format")
    private String phone;

    @jakarta.validation.constraints.Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private boolean isActive = true;

    @Size(max = 255, message = "Opening hours must not exceed 255 characters")
    private String openingHours;

    @Size(max = 100, message = "Manager name must not exceed 100 characters")
    private String managerName;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private String parentLocationId;

    // For vehicles only
    @Size(max = 20, message = "Vehicle registration must not exceed 20 characters")
    private String vehicleRegistration;

    private Integer vehicleCapacity;
}
