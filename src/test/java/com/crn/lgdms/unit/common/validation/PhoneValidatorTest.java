package com.crn.lgdms.unit.common.validation;

import com.crn.lgdms.common.validation.validators.PhoneValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PhoneValidatorTest {

    private PhoneValidator phoneValidator;

    @BeforeEach
    void setUp() {
        phoneValidator = new PhoneValidator();
    }

    @Test
    void shouldValidateTanzanianPhoneNumbers() {
        assertTrue(phoneValidator.isValid("0712345678", null));
        assertTrue(phoneValidator.isValid("0612345678", null));
        assertTrue(phoneValidator.isValid("+255712345678", null));
        assertTrue(phoneValidator.isValid("+255612345678", null));
    }

    @Test
    void shouldRejectInvalidPhoneNumbers() {
        assertFalse(phoneValidator.isValid("12345", null));
        assertFalse(phoneValidator.isValid("071234567", null)); // Too short
        assertFalse(phoneValidator.isValid("07123456789", null)); // Too long
        assertFalse(phoneValidator.isValid("0812345678", null)); // Invalid prefix
        assertFalse(phoneValidator.isValid("+25571234567", null)); // Too short
        assertFalse(phoneValidator.isValid("abc", null));
        assertFalse(phoneValidator.isValid("", null));
    }

    @Test
    void shouldAcceptNullAsValid() {
        assertTrue(phoneValidator.isValid(null, null));
    }
}
