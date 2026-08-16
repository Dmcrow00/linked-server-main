package com.linked.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AppUtils {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private AppUtils() {
        throw new UnsupportedOperationException("Clase utilitaria - no instanciable");
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_FORMAT) : "";
    }

    public static String sanitizeSearch(String query) {
        if (query == null) return "";
        return query.trim().replaceAll("\\s+", " ");
    }
}
