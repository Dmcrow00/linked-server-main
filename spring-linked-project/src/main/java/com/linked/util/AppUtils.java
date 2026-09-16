package com.linked.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Utilidades generales sin estado. */
public final class AppUtils {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private AppUtils() {
        throw new UnsupportedOperationException("Clase utilitaria - no instanciable");
    }

    /** Formatea la fecha como {@code dd/MM/yyyy HH:mm:ss}; devuelve cadena vacía si es {@code null}. */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_FORMAT) : "";
    }

    /** Recorta el texto de búsqueda y colapsa los espacios repetidos. */
    public static String sanitizeSearch(String query) {
        if (query == null) return "";
        return query.trim().replaceAll("\\s+", " ");
    }
}
