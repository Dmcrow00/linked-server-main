package com.linked.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Formato de las fechas que devuelve la API: ISO-8601 local ({@code 2026-10-15},
 * {@code 2026-10-15T18:30:00}). Los mappers lo declaran en {@code uses} y MapStruct lo aplica a
 * toda conversión de fecha a texto.
 */
public final class FormatoDeFechas {

    private FormatoDeFechas() {
    }

    public static String fecha(LocalDate fecha) {
        return fecha != null ? fecha.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    public static String fechaHora(LocalDateTime fechaHora) {
        return fechaHora != null ? fechaHora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
}
