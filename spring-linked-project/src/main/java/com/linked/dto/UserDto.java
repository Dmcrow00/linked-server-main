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
