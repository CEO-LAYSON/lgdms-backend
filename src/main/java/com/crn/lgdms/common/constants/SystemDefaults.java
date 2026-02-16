package com.crn.lgdms.common.constants;

import java.math.BigDecimal;

public final class SystemDefaults {
    public static final String DEFAULT_CURRENCY = "TZS";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final BigDecimal DEFAULT_CREDIT_LIMIT = BigDecimal.valueOf(1000000);
    public static final int SESSION_TIMEOUT_MINUTES = 30;

    private SystemDefaults() {}
}
