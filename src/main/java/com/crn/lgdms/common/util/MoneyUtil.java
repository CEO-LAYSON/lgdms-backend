package com.crn.lgdms.common.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MoneyUtil {

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return safe(a).add(safe(b)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return safe(a).subtract(safe(b)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return safe(a).multiply(safe(b)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (b == null || b.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return safe(a).divide(safe(b), 2, RoundingMode.HALF_UP);
    }

    public static boolean isGreaterThan(BigDecimal a, BigDecimal b) {
        return safe(a).compareTo(safe(b)) > 0;
    }

    public static boolean isLessThan(BigDecimal a, BigDecimal b) {
        return safe(a).compareTo(safe(b)) < 0;
    }

    public static boolean isZero(BigDecimal value) {
        return safe(value).compareTo(BigDecimal.ZERO) == 0;
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
