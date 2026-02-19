package com.crn.lgdms.fixtures;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Fixtures {

    public static final String TEST_USER_ID = "test-user-id";
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_PASSWORD = "Password123!";
    public static final String TEST_EMAIL = "test@example.com";

    public static final String TEST_LOCATION_ID = "test-location-id";
    public static final String TEST_LOCATION_NAME = "Test Branch";

    public static final String TEST_CYLINDER_3KG_ID = "cylinder-3kg-id";
    public static final String TEST_CYLINDER_6KG_ID = "cylinder-6kg-id";
    public static final String TEST_CYLINDER_15KG_ID = "cylinder-15kg-id";

    public static final BigDecimal PRICE_3KG_COMPLETE = BigDecimal.valueOf(35000);
    public static final BigDecimal PRICE_3KG_REFILL = BigDecimal.valueOf(25000);
    public static final BigDecimal PRICE_6KG_COMPLETE = BigDecimal.valueOf(65000);
    public static final BigDecimal PRICE_6KG_REFILL = BigDecimal.valueOf(50000);
    public static final BigDecimal PRICE_15KG_COMPLETE = BigDecimal.valueOf(150000);
    public static final BigDecimal PRICE_15KG_REFILL = BigDecimal.valueOf(120000);

    public static Map<String, Object> createSaleRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("locationId", TEST_LOCATION_ID);
        request.put("salesPersonId", TEST_USER_ID);
        request.put("saleDate", "2024-01-30");

        Map<String, Object> item = new HashMap<>();
        item.put("cylinderSizeId", TEST_CYLINDER_15KG_ID);
        item.put("productType", "COMPLETE");
        item.put("quantity", 2);
        item.put("unitPrice", PRICE_15KG_COMPLETE);
        item.put("emptyReturned", false);

        request.put("items", new Object[]{item});

        Map<String, Object> payment = new HashMap<>();
        payment.put("paymentMethod", "CASH");
        payment.put("amount", PRICE_15KG_COMPLETE.multiply(BigDecimal.valueOf(2)));

        request.put("payments", new Object[]{payment});

        return request;
    }
}
