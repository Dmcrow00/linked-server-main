package com.linked.dto;

/** Contratos de solicitudes para ser tatuador ({@code /api/solicitudes-tatuador}). */
public class SolicitudTatuadorDto {

    public record RejectRequest(
            String notas
    ) {}

    /**
     * @param especialidadesIds arreglo JSON con los IDs de especialidades pedidas (ej. {@code "[1, 3]"})
     * @param status            pendiente, aprobada o rechazada
     */
    public record Response(
            Long id,
            UserDto.Response user,
            String estudio,
            String especialidadesIds,
            String status,
            String notas,
            String adminUser,
            String creationTimestamp
    ) {}
}
