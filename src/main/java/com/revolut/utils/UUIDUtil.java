package com.revolut.utils;

import java.util.UUID;

/**
 * UUID utilities
 */
public class UUIDUtil {
    /**
     * Parse and validate UUID from string
     *
     * @param value string containing UUID
     *
     * @return UUID if valid, null otherwise
     */
    public static UUID parse(String value) {
        try {
            UUID uuid = UUID.fromString(value);
            return uuid.toString().equals(value) ? uuid : null;
        } catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }
}
