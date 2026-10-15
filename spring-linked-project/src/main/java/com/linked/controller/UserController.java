package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.AuthDto;
import com.linked.dto.SweRolDto;
import com.linked.dto.UserDto;
import com.linked.mapper.SweUsrMapper;
import com.linked.repository.SweRolRepository;
import com.linked.repository.SweUsrRolRepository;
import com.linked.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.linked.exception.AppExceptions;

import java.net.URI;
import java.util.List;

/** Registro, perfil y administración de usuarios y sus roles. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios")
public class UserController {

    private final UserService userService;
    private final SweRolRepository sweRolRepository;
    private final SweUsrRolRepository sweUsrRolRepository;
    private final SweUsrMapper sweUsrMapper;

    @GetMapping
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<ApiResponse<List<UserDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(userService.findAll()));
    }

    @GetMapping("/tatuadores")
    @Operation(summary = "Listar tatuadores activos")
    public ResponseEntity<ApiResponse<List<UserDto.Response>>> findAllTatuadores() {
        return ResponseEntity.ok(ApiResponse.ok(userService.findAllTatuadores()));
    }

    @GetMapping("/getAllRoles")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Listar todos los roles")
    public ResponseEntity<ApiResponse<List<SweRolDto.Response>>> getAllRoles() {
        List<SweRolDto.Response> roles = sweRolRepository.findAll().stream()
                .map(r -> new SweRolDto.Response(r.getId(), r.getDescription(), r.getStatus()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(roles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<ApiResponse<UserDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }

    /** Alimenta el portfolio público: cualquiera puede consultarlo, con o sin sesión. */
    @GetMapping("/{id}/publico")
    @Operation(summary = "Perfil público de un tatuador (no requiere sesión)")
    public ResponseEntity<ApiResponse<UserDto.PerfilPublico>> findPerfilPublico(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findPerfilPublico(id)));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario (Cliente)")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> register(
            @Valid @RequestBody UserDto.CreateRequest request) {
        AuthDto.TokenResponse response = userService.register(request);
        return ResponseEntity.created(URI.create("/api/users")).body(ApiResponse.created(response));
    }

    @PostMapping("/register-tatuador")
    @Operation(summary = "Solicitar registro como tatuador")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> registerTatuador(
            @Valid @RequestBody UserDto.RegisterTatuadorRequest request) {
        AuthDto.TokenResponse response = userService.registerTatuador(request);
        return ResponseEntity.created(URI.create("/api/users")).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar perfil de usuario")
    public ResponseEntity<ApiResponse<UserDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.update(id, request)));
    }

    @PatchMapping("/{id}/profile-photo")
    @Operation(summary = "Actualizar foto de perfil")
    public ResponseEntity<ApiResponse<UserDto.Response>> updateProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfilePhoto(id, file)));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Actualizar roles de un usuario")
    public ResponseEntity<ApiResponse<Void>> updateRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserDto.UpdateRolesRequest request) {
        userService.updateRoles(id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar reset de contraseña")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody UserDto.ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña con token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody UserDto.ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/getUserRolsByIdUser/{idUser}")
    @Operation(summary = "Obtener IDs de roles asignados a un usuario")
    public ResponseEntity<ApiResponse<List<Long>>> getUserRolsByIdUser(@PathVariable Long idUser) {
        List<Long> roleIds = sweUsrRolRepository.findByUsrapl_Id(idUser).stream()
                .filter(ur -> "true".equals(ur.getStatus()))
                .map(ur -> ur.getRolapl().getId())
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(roleIds));
    }

    @GetMapping("/getUserRolByidRole/{idRole}")
    @Operation(summary = "Obtener rol por ID")
    public ResponseEntity<ApiResponse<SweRolDto.Response>> getUserRolByidRole(@PathVariable Long idRole) {
        SweRolDto.Response rol = sweRolRepository.findById(idRole)
                .map(r -> new SweRolDto.Response(r.getId(), r.getDescription(), r.getStatus()))
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Rol", "id", idRole));
        return ResponseEntity.ok(ApiResponse.ok(rol));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Eliminar usuario (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
