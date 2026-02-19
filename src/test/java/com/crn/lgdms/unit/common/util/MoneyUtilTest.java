package com.crn.lgdms.unit.common.util;

import com.crn.lgdms.common.util.MoneyUtil;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class MoneyUtilTest {

    @Test
    void shouldAddTwoAmounts() {
        BigDecimal a = BigDecimal.valueOf(100.50);
        BigDecimal b = BigDecimal.valueOf(200.75);

        BigDecimal result = MoneyUtil.add(a, b);

        assertEquals(BigDecimal.valueOf(301.25), result);
    }

    @Test
    void shouldHandleNullInAdd() {
        BigDecimal result = MoneyUtil.add(null, BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(100), result);

        result = MoneyUtil.add(BigDecimal.valueOf(100), null);
        assertEquals(BigDecimal.valueOf(100), result);

        result = MoneyUtil.add(null, null);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void shouldSubtractTwoAmounts() {
        BigDecimal a = BigDecimal.valueOf(300.00);
        BigDecimal b = BigDecimal.valueOf(150.50);

        BigDecimal result = MoneyUtil.subtract(a, b);

        assertEquals(BigDecimal.valueOf(149.50), result);
    }

    @Test
    void shouldCompareAmounts() {
        assertTrue(MoneyUtil.isGreaterThan(BigDecimal.valueOf(200), BigDecimal.valueOf(100)));
        assertFalse(MoneyUtil.isGreaterThan(BigDecimal.valueOf(100), BigDecimal.valueOf(200)));

        assertTrue(MoneyUtil.isLessThan(BigDecimal.valueOf(50), BigDecimal.valueOf(100)));
        assertFalse(MoneyUtil.isLessThan(BigDecimal.valueOf(100), BigDecimal.valueOf(50)));
    }

    @Test
    void shouldCheckIfZero() {
        assertTrue(MoneyUtil.isZero(BigDecimal.ZERO));
        assertTrue(MoneyUtil.isZero(null));
        assertFalse(MoneyUtil.isZero(BigDecimal.valueOf(0.01)));
    }
}
