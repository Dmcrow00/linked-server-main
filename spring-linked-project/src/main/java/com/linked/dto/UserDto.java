package com.linked.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class UserDto {

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
