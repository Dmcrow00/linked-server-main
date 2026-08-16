package com.linked.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class TurnoSesionDto {

    public record CreateRequest(
            @NotNull Long tatuadorId,
            @NotNull String fechaHoraInicio,
            @NotNull String fechaHoraFin,
            String descripcionCliente
    ) {}

    public record GestionarRequest(
            @NotNull String estado
    ) {}

    public record UpdateRequest(
            String fechaHoraInicio,
            String fechaHoraFin,
            String descripcionCliente,
            String estado
    ) {}

    public record Response(
            Long id,
            UserDto.Response cliente,
            String estado,
            String fechaHoraInicio,
            String fechaHoraFin,
            String descripcionCliente,
            List<UserDto.Response> tatuadores,
            List<String> imagenesRef,
            List<TurnoMensajeDto.Response> mensajes,
            String creationTimestamp
    ) {}
}
