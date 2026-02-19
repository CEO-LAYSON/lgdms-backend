package com.crn.lgdms.modules.locations.domain.entity;

import com.crn.lgdms.common.enums.LocationType;
import com.crn.lgdms.modules.users.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "locations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    private LocationType locationType;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "opening_hours", length = 255)
    private String openingHours;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "parent_location_id")
    private String parentLocationId; // For hierarchical relationships if needed

    // For vehicles only
    @Column(name = "vehicle_registration", length = 20)
    private String vehicleRegistration;

    @Column(name = "vehicle_capacity")
    private Integer vehicleCapacity; // Number of cylinders vehicle can carry
}
