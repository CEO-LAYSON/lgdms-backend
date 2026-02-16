package com.crn.lgdms.common.validation.validators;

import com.crn.lgdms.common.validation.annotations.ValidPhone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private static final Pattern TZ_PHONE_PATTERN =
        Pattern.compile("^(\\+255|0)[67]\\d{8}$");

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }
        return TZ_PHONE_PATTERN.matcher(phone.trim()).matches();
    }
}
