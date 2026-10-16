package com.linked.service;

import com.dmc00.core.swe.dto.AuthDto;
import com.dmc00.core.swe.dto.SweRolDto;
import com.linked.dto.EspecialidadDto;
import com.linked.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Usuarios de linked: la cuenta la maneja core-swe ({@code SweUsrService}); este servicio agrega
 * el perfil propio de la app, el registro de tatuadores y los mails.
 */
public interface UserService {

    /** Todos los usuarios, también los dados de baja (pantalla de administración). */
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

    /** Actualización parcial: los campos {@code null} no se modifican. */
    UserDto.Response update(Long id, UserDto.UpdateRequest request);

    /**
     * Sube la foto a Cloudinary y guarda su URL. Si el usuario vinculó Instagram la foto es la de
     * Instagram y no se puede cambiar a mano (403).
     */
    UserDto.Response updateProfilePhoto(Long id, MultipartFile file);

    /** Reemplaza todos los roles del usuario por los indicados. */
    void updateRoles(Long id, UserDto.UpdateRolesRequest request);

    List<SweRolDto.Response> findAllRoles();

    /** Ids de los roles vigentes del usuario. */
    List<Long> findRoleIdsByUser(Long userId);

    SweRolDto.Response findRoleById(Long roleId);

    List<EspecialidadDto.Response> findEspecialidades(Long tatuadorId);

    /** Reemplaza las especialidades del tatuador; los ids inexistentes se ignoran. */
    void assignEspecialidades(Long tatuadorId, List<Long> especialidadIds);

    void removeEspecialidad(Long tatuadorId, Long especialidadId);

    /** Si el email existe, manda el enlace de recuperación; si no, no hace nada (no se informa). */
    void forgotPassword(UserDto.ForgotPasswordRequest request);

    void resetPassword(UserDto.ResetPasswordRequest request);

    /** Baja lógica: el usuario ya no puede iniciar sesión. */
    void delete(Long id);
}
