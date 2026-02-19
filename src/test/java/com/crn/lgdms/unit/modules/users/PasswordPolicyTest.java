package com.crn.lgdms.unit.modules.users;

import com.crn.lgdms.modules.users.domain.valueobject.PasswordPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordPolicyTest {

    private PasswordPolicy passwordPolicy;

    @BeforeEach
    void setUp() {
        passwordPolicy = new PasswordPolicy();
    }

    @Test
    void shouldValidateStrongPassword() {
        assertTrue(passwordPolicy.isValid("Password123!"));
        assertTrue(passwordPolicy.isValid("StrongP@ssw0rd"));
        assertTrue(passwordPolicy.isValid("Test1234$"));
    }

    @Test
    void shouldRejectWeakPasswords() {
        assertFalse(passwordPolicy.isValid("password")); // No uppercase, numbers, special
        assertFalse(passwordPolicy.isValid("PASSWORD")); // No lowercase, numbers, special
        assertFalse(passwordPolicy.isValid("Pass123")); // Too short
        assertFalse(passwordPolicy.isValid("Password")); // No numbers or special
        assertFalse(passwordPolicy.isValid("password123")); // No uppercase or special
        assertFalse(passwordPolicy.isValid("")); // Empty
        assertFalse(passwordPolicy.isValid(null)); // Null
    }

    @Test
    void shouldProvidePolicyDescription() {
        String description = passwordPolicy.getPolicyDescription();
        assertNotNull(description);
        assertTrue(description.contains("8 characters"));
        assertTrue(description.contains("uppercase"));
        assertTrue(description.contains("lowercase"));
        assertTrue(description.contains("number"));
        assertTrue(description.contains("special character"));
    }
}
