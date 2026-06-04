package com.example.app;

import android.util.Base64;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;

public class Refactor {

    // Regex constants matching standard NIRA and telecom formats
    private static final Pattern NIN_PATTERN = Pattern.compile("^CM[0-9A-Z]{11}$|^CW[0-9A-Z]{11}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+256[47]\\d{8}$");

    // Enforce standardization via a structured Data Object
    public static class StandardData {
        public final String nin;
        public final String phone;

        public StandardData(String nin, String phone) {
            if (!isValidNIN(nin)) {
                throw new IllegalArgumentException("Invalid NIN format. Must be 14 characters starting with CM or CW.");
            }
            if (!isValidPhone(phone)) {
                throw new IllegalArgumentException("Invalid phone format. Must be international standard (e.g., +2567XXXXXXXX).");
            }
            this.nin = nin.toUpperCase().trim();
            this.phone = phone.trim();
        }
    }


    // Validation Engines
    public static boolean isValidNIN(String nin) {
        return nin != null && NIN_PATTERN.matcher(nin.toUpperCase().trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }
}

