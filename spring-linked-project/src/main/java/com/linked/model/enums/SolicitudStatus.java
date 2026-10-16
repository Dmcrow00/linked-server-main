package com.linked.model.enums;

/**
 * Estado de una solicitud para ser tatuador.
 * <p>
 * Se persiste por nombre ({@code EnumType.STRING}), por eso los valores van en minúscula:
 * coinciden con los datos existentes en {@code tat_solicitud_tatuador}.
 */
public enum SolicitudStatus {
    pendiente,
    aprobada,
    rechazada
}
