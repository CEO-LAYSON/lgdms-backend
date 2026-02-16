package com.crn.lgdms.common.util;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class IdUtil {

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    public static String generateShortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public static boolean isValidUuid(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
