package com.crn.lgdms.modules.users.domain.valueobject;

import lombok.Value;

@Value
public class PasswordPolicy {
    int minLength = 8;
    boolean requireUppercase = true;
    boolean requireLowercase = true;
    boolean requireNumbers = true;
    boolean requireSpecialChars = true;
    int historyCount = 5;
    int expiryDays = 90;

    public String getPolicyDescription() {
        return String.format(
            "Password must be at least %d characters long and contain at least one " +
                "uppercase letter, one lowercase letter, one number, and one special character.",
            minLength
        );
    }

    public boolean isValid(String password) {
        if (password == null || password.length() < minLength) {
            return false;
        }

        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            return false;
        }

        if (requireLowercase && !password.matches(".*[a-z].*")) {
            return false;
        }

        if (requireNumbers && !password.matches(".*\\d.*")) {
            return false;
        }

        if (requireSpecialChars && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return false;
        }

        return true;
    }
}
