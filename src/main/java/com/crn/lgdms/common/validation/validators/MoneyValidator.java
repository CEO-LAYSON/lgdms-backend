package com.crn.lgdms.common.validation.validators;

import com.crn.lgdms.common.validation.annotations.ValidMoney;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class MoneyValidator implements ConstraintValidator<ValidMoney, BigDecimal> {

    private double min;
    private double max;

    @Override
    public void initialize(ValidMoney annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        double doubleValue = value.doubleValue();
        return doubleValue >= min && doubleValue <= max;
    }
}
