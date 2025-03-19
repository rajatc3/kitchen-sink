package org.johndoe.kitchensink.utils;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for common tasks.
 */
public class UtilityMethods {

    /**
     * Masks an email address by replacing characters with asterisks.
     *
     * @param email the email address to mask
     * @return the masked email address
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "*****";

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return username.charAt(0) + "****@" + domain;
        }

        return username.charAt(0) + "****" + username.charAt(username.length() - 1) + "@" + domain;
    }

    /**
     * Masks a phone number by replacing characters with asterisks.
     *
     * @param phone the phone number to mask
     * @return the masked phone number
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "****";

        int visibleDigits = 2;
        String start = phone.substring(0, visibleDigits);
        String end = phone.substring(phone.length() - visibleDigits);

        return start + "****" + end;
    }

    public static Map<String, Object> paginateResponse(Page<?> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent()); // DTO list
        response.put("currentPage", page.getNumber());
        response.put("totalPages", page.getTotalPages());
        response.put("totalElements", page.getTotalElements());
        response.put("pageSize", page.getSize());
        response.put("isLast", page.isLast());
        return response;
    }
}
