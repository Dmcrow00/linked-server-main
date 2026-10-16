package com.linked.controller;

import com.dmc00.core.swe.dto.AuthDto;
import com.dmc00.core.swe.dto.SweRolDto;
import com.dmc00.core.web.ApiResponse;
import com.linked.dto.UserDto;
import com.linked.security.Permisos;
import com.linked.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/**
 * Registro, perfil y administración de usuarios y sus roles. El login, la clave pública y la
 * disponibilidad de username/email son de core-swe ({@code /api/auth}).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<ApiResponse<List<UserDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(userService.findAll()));
    }

    @GetMapping("/tatuadores")
    @PreAuthorize(Permisos.AUTENTICADO)
    @Operation(summary = "Listar tatuadores activos")
    public ResponseEntity<ApiResponse<List<UserDto.Response>>> findAllTatuadores() {
        return ResponseEntity.ok(ApiResponse.ok(userService.findAllTatuadores()));
    }

    @GetMapping("/getAllRoles")
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Listar todos los roles")
    public ResponseEntity<ApiResponse<List<SweRolDto.Response>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.ok(userService.findAllRoles()));
    }

    @GetMapping("/{id}")
    @PreAuthorize(Permisos.AUTENTICADO)
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<ApiResponse<UserDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }

    /** Alimenta el portfolio público: cualquiera puede consultarlo, con o sin sesión. */
    @GetMapping("/{id}/publico")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Perfil público de un tatuador (no requiere sesión)")
    public ResponseEntity<ApiResponse<UserDto.PerfilPublico>> findPerfilPublico(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findPerfilPublico(id)));
    }

    @PostMapping("/register")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Registrar nuevo usuario (Cliente)")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> register(
            @Valid @RequestBody UserDto.CreateRequest request) {
        AuthDto.TokenResponse response = userService.register(request);
        return ResponseEntity.created(URI.create("/api/users/" + response.userId())).body(ApiResponse.created(response));
    }

    @PostMapping("/register-tatuador")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Solicitar registro como tatuador")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> registerTatuador(
            @Valid @RequestBody UserDto.RegisterTatuadorRequest request) {
        AuthDto.TokenResponse response = userService.registerTatuador(request);
        return ResponseEntity.created(URI.create("/api/users/" + response.userId())).body(ApiResponse.created(response));
    }

    /**
     * Cada usuario edita su perfil y el administrador el de cualquiera. Reemplaza también al
     * {@code PUT /api/users/updateUser/:id} del backend Node (ver docs/documentacion-tecnica.md).
     */
    @PutMapping("/{id}")
    @PreAuthorize(Permisos.EL_MISMO_O_ADMINISTRADOR)
    @Operation(summary = "Actualizar perfil de usuario (el propio o, si es administrador, cualquiera)")
    public ResponseEntity<ApiResponse<UserDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.update(id, request)));
    }

    @PatchMapping("/{id}/profile-photo")
    @PreAuthorize(Permisos.EL_MISMO_O_ADMINISTRADOR)
    @Operation(summary = "Actualizar foto de perfil")
    public ResponseEntity<ApiResponse<UserDto.Response>> updateProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfilePhoto(id, file)));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Actualizar roles de un usuario")
    public ResponseEntity<ApiResponse<Void>> updateRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserDto.UpdateRolesRequest request) {
        userService.updateRoles(id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/forgot-password")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Solicitar reset de contraseña")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody UserDto.ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Si el email está registrado, se envió un correo de recuperación"));
    }

    @PostMapping("/reset-password")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Restablecer contraseña con token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody UserDto.ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Contraseña actualizada correctamente"));
    }

    @GetMapping("/getUserRolsByIdUser/{idUser}")
    @PreAuthorize(Permisos.AUTENTICADO)
    @Operation(summary = "Obtener IDs de roles asignados a un usuario")
    public ResponseEntity<ApiResponse<List<Long>>> getUserRolsByIdUser(@PathVariable Long idUser) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findRoleIdsByUser(idUser)));
    }

    @GetMapping("/getUserRolByidRole/{idRole}")
    @PreAuthorize(Permisos.AUTENTICADO)
    @Operation(summary = "Obtener rol por ID")
    public ResponseEntity<ApiResponse<SweRolDto.Response>> getUserRolByidRole(@PathVariable Long idRole) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findRoleById(idRole)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Eliminar usuario (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
