package com.crn.lgdms.modules.locations.dto.response;

import com.crn.lgdms.common.enums.LocationType;
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
public class LocationResponse {
    private String id;
    private String name;
    private String code;
    private LocationType locationType;
    private String address;
    private String city;
    private String phone;
    private String email;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean isActive;
    private String openingHours;
    private String managerName;
    private String notes;
    private String parentLocationId;
    private String vehicleRegistration;
    private Integer vehicleCapacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
