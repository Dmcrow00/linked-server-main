package com.linked.model.enums;

/**
 * Estados posibles de un turno.
 * <p>
 * Se guardan como texto en {@code tat_turno_sesion.estado}; los nombres coinciden
 * exactamente (con mayúscula inicial) con los valores existentes en base de datos.
 */
public enum EstadoTurno {
    Pendiente,
    Confirmada,
    Cancelada,
    Completada,
    Rechazada,
    Propuesta
}
