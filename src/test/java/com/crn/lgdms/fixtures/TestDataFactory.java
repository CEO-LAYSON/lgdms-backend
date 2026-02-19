package com.crn.lgdms.fixtures;

import com.crn.lgdms.common.enums.LocationType;
import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.domain.entity.Supplier;
import com.crn.lgdms.modules.sales.domain.entity.Customer;
import com.crn.lgdms.modules.users.domain.entity.Role;
import com.crn.lgdms.modules.users.domain.entity.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

public class TestDataFactory {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static User createTestUser() {
        return User.builder()
            .id(UUID.randomUUID().toString())
            .username("testuser")
            .password(encoder.encode("password"))
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .phone("0712345678")
            .isActive(true)
            .roles(new HashSet<>())
            .build();
    }

    public static Role createTestRole(String name) {
        return Role.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .description("Test role")
            .isSystemRole(false)
            .permissions(new HashSet<>())
            .users(new HashSet<>())
            .build();
    }

    public static Location createTestLocation(String name, LocationType type) {
        return Location.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .code(type.toString() + "001")
            .locationType(type)
            .address("123 Test St")
            .city("Dar es Salaam")
            .phone("0712345678")
            .isActive(true)
            .build();
    }

    public static CylinderSize createTestCylinderSize(String name, BigDecimal weight) {
        return CylinderSize.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .weightKg(weight)
            .tareWeightKg(weight.add(BigDecimal.valueOf(0.5)))
            .isActive(true)
            .displayOrder(1)
            .build();
    }

    public static Supplier createTestSupplier(String name) {
        return Supplier.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .code("SUP001")
            .contactPerson("John Supplier")
            .phone("0712345678")
            .email("supplier@example.com")
            .isActive(true)
            .build();
    }

    public static Customer createTestCustomer(String name) {
        return Customer.builder()
            .id(UUID.randomUUID().toString())
            .customerNumber("CUST-001")
            .name(name)
            .phone("0712345678")
            .email(name.toLowerCase() + "@example.com")
            .customerType(Customer.CustomerType.RETAIL)
            .creditLimit(BigDecimal.valueOf(1000000))
            .currentBalance(BigDecimal.ZERO)
            .isActive(true)
            .build();
    }

    public static LocalDate getTestDate() {
        return LocalDate.now();
    }
}
