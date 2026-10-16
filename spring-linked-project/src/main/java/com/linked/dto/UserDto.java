package com.linked.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/** Contratos de usuarios ({@code /api/users}). Las contraseñas llegan cifradas con la clave pública RSA. */
public class UserDto {

    /** Registro de un cliente. */
    public record CreateRequest(
            @NotBlank @Size(max = 25) String username,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 8, max = 512) String password,
            @Size(max = 25) String realname,
            @Size(max = 25) String surname,
            @NotNull @JsonProperty("birth_date") LocalDate birthDate,
            String localidad,
            Double lat,
            Double lng,
            String recaptchaToken
    ) {}

    /**
     * Registro como tatuador: el usuario se crea como Cliente junto con una solicitud
     * pendiente que incluye el estudio y las especialidades elegidas.
     */
    public record RegisterTatuadorRequest(
            @NotBlank @Size(max = 25) String username,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 8, max = 512) String password,
            @Size(max = 25) String realname,
            @Size(max = 25) String surname,
            @NotNull @JsonProperty("birth_date") LocalDate birthDate,
            @Size(max = 100) String estudio,
            @JsonProperty("especialidadIds") List<Long> especialidadesIds,
            String localidad,
            Double lat,
            Double lng,
            String recaptchaToken
    ) {}

    /** Actualización parcial del perfil: los campos {@code null} no se modifican. */
    public record UpdateRequest(
            @Size(max = 25) String realname,
            @Size(max = 25) String surname,
            LocalDate birthDate,
            @Size(max = 100) String estudio,
            LocalDate fechaInicioActividad,
            @Size(max = 255) String localidad,
            Double lat,
            Double lng,
            @Size(max = 100) String instagramHandle
    ) {}

    /** Reemplaza todos los roles del usuario por los indicados. */
    public record UpdateRolesRequest(
            @NotNull List<Long> roleIds
    ) {}

    public record ForgotPasswordRequest(
            @NotBlank @Email String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 512) String newPassword
    ) {}

    /**
     * Vista pública de un tatuador, para respuestas que se sirven sin sesión.
     * <p>
     * Deliberadamente no incluye email, fecha de nacimiento, ubicación, roles ni datos de
     * contacto: esos solo viajan en {@link Response}, que siempre requiere autenticación.
     */
    public record Publico(
            Long id,
            String username,
            String profilePhoto,
            String estudio
    ) {}

    /**
     * Perfil público de un tatuador: lo que muestra su portfolio sin sesión iniciada.
     * <p>
     * Como {@link Publico}, no expone nombre real, contacto ni ubicación; suma lo que describe
     * su trabajo. La antigüedad va en años cumplidos para no publicar la fecha exacta de inicio.
     */
    public record PerfilPublico(
            Long id,
            String username,
            String profilePhoto,
            String estudio,
            Integer antiguedad,
            List<EspecialidadDto.Response> especialidades
    ) {}

    /** Perfil de un usuario con sus roles vigentes y especialidades. Fechas en formato {@code yyyy-MM-dd}. */
    public record Response(
            Long id,
            String username,
            String email,
            String realname,
            String surname,
            String birthDate,
            String profilePhoto,
            String estudio,
            String fechaInicioActividad,
            String localidad,
            Double lat,
            Double lng,
            String instagramHandle,
            String status,
            List<SweRolDto.Response> roles,
            List<EspecialidadDto.Response> especialidades
    ) {}
}
