package com.crn.lgdms.common.validation.annotations;

import com.crn.lgdms.common.validation.validators.MoneyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MoneyValidator.class)
@Documented
public @interface ValidMoney {
    String message() default "Invalid amount format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    double min() default 0;
    double max() default Double.MAX_VALUE;
}
