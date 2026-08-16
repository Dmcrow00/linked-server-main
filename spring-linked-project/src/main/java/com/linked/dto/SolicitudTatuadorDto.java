package com.linked.dto;

public class SolicitudTatuadorDto {

    public record RejectRequest(
            String notas
    ) {}

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
