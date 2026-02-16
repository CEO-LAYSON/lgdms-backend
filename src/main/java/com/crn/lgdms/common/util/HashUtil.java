package com.crn.lgdms.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HashUtil {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static String hash(String rawValue) {
        return ENCODER.encode(rawValue);
    }

    public static boolean matches(String rawValue, String hashedValue) {
        return ENCODER.matches(rawValue, hashedValue);
    }

    public static String generateApiKey() {
        return java.util.UUID.randomUUID().toString()
            .replace("-", "")
            .toUpperCase();
    }
}
