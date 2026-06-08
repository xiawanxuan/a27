package com.herbarium.common.util;

import java.util.UUID;

public class IdUtils {

    private IdUtils() {
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateSpecimenNo() {
        String dateStr = DateUtils.formatDate(java.time.LocalDate.now()).replace("-", "");
        String random = generateUUID().substring(0, 8).toUpperCase();
        return "SP" + dateStr + random;
    }
}
