package com.linked.service;

import com.linked.dto.AuthDto;
import com.linked.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Alta, perfil, roles, especialidades y recuperación de contraseña de usuarios. */
public interface UserService {

    List<UserDto.Response> findAll();

    List<UserDto.Response> findAllTatuadores();

    UserDto.Response findById(Long id);

    /** Registra un cliente y devuelve su JWT para que quede con la sesión iniciada. */
    AuthDto.TokenResponse register(UserDto.CreateRequest request);

    /**
     * Registra al usuario como Cliente y crea su solicitud para ser tatuador, que queda
     * pendiente de aprobación por un administrador.
     */
    AuthDto.TokenResponse registerTatuador(UserDto.RegisterTatuadorRequest request);

    /** Perfil público de un tatuador para su portfolio; no requiere sesión. */
    UserDto.PerfilPublico findPerfilPublico(Long id);

    UserDto.Response update(Long id, UserDto.UpdateRequest request);

    /** Sube la foto a Cloudinary y guarda su URL en el perfil. */
    UserDto.Response updateProfilePhoto(Long id, MultipartFile file);

    /** Reemplaza todos los roles del usuario por los indicados. */
    void updateRoles(Long id, UserDto.UpdateRolesRequest request);

    /** Reemplaza las especialidades del tatuador; los IDs inexistentes se ignoran. */
    void assignEspecialidades(Long tatuadorId, List<Long> especialidadIds);

    void removeEspecialidad(Long tatuadorId, Long especialidadId);

    /** Genera un token de recuperación válido por 1 hora y lo envía por email. */
    void forgotPassword(UserDto.ForgotPasswordRequest request);

    /** Cambia la contraseña si el token existe y no venció; después el token queda invalidado. */
    void resetPassword(UserDto.ResetPasswordRequest request);

    /** Baja lógica: registra la fecha de baja y deshabilita al usuario, que ya no puede iniciar sesión. */
    void delete(Long id);
}
