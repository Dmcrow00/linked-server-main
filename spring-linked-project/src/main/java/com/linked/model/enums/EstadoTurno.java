package com.linked.model.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Estados posibles de un turno (diagrama en docs/diagramas/estados.md).
 * <p>
 * Se guardan como texto en {@code tat_turno_sesion.estado}; los nombres coinciden
 * exactamente (con mayúscula inicial) con los valores existentes en base de datos.
 * <p>
 * Como en el backend Node, la única regla de transición es que un turno completado o
 * cancelado ya no cambia; entre los demás estados el tatuador puede ir y volver (p. ej.
 * restaurar a Pendiente uno que rechazó).
 */
public enum EstadoTurno {
    Pendiente,
    Confirmada,
    Cancelada,
    Completada,
    Rechazada,
    Propuesta;

    /** Completada y Cancelada cierran el turno. */
    public boolean esFinal() {
        return this == Completada || this == Cancelada;
    }

    public boolean puedePasarA(EstadoTurno destino) {
        return !esFinal() && destino != null;
    }

    /** El estado con ese nombre exacto, o vacío si no existe. */
    public static Optional<EstadoTurno> desde(String nombre) {
        return Arrays.stream(values()).filter(estado -> estado.name().equals(nombre)).findFirst();
    }
}
