package com.crn.lgdms.common.mapping;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class MoneyMapper {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public BigDecimal toBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value).setScale(SCALE, ROUNDING_MODE);
    }

    public String toString(BigDecimal value) {
        return value != null ? value.setScale(SCALE, ROUNDING_MODE).toString() : "0.00";
    }

    public String toFormattedString(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("sw", "TZ"));
        return formatter.format(value.setScale(SCALE, ROUNDING_MODE));
    }
}
