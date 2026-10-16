package com.linked.service.impl;

import com.dmc00.core.data.BaseEntity;
import com.dmc00.core.security.RecaptchaService;
import com.dmc00.core.swe.dto.AuthDto;
import com.dmc00.core.swe.dto.SweRolDto;
import com.dmc00.core.swe.dto.SweUsrDto;
import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.swe.repository.SweRolRepository;
import com.dmc00.core.swe.repository.SweUsrRepository;
import com.dmc00.core.swe.service.AuthService;
import com.dmc00.core.swe.service.SweUsrService;
import com.dmc00.core.web.AppExceptions;
import com.linked.dto.EspecialidadDto;
import com.linked.dto.UserDto;
import com.linked.integration.AlmacenDeImagenes;
import com.linked.mapper.EspecialidadMapper;
import com.linked.mapper.UsuarioMapper;
import com.linked.model.entity.PerfilUsuario;
import com.linked.model.entity.SolicitudTatuador;
import com.linked.repository.EspecialidadRepository;
import com.linked.repository.InstagramTokenRepository;
import com.linked.repository.PerfilUsuarioRepository;
import com.linked.repository.SolicitudTatuadorRepository;
import com.linked.service.EmailService;
import com.linked.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

/** Implementación de {@link UserService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    static final String CLIENTE = "Cliente";
    static final String TATUADOR = "Tatuador";

    private final SweUsrService sweUsrService;
    private final AuthService authService;
    private final RecaptchaService recaptchaService;
    private final SweUsrRepository usuarios;
    private final SweRolRepository roles;
    private final PerfilUsuarioRepository perfiles;
    private final EspecialidadRepository especialidades;
    private final SolicitudTatuadorRepository solicitudes;
    private final InstagramTokenRepository tokensDeInstagram;
    private final UsuarioMapper usuarioMapper;
    private final EspecialidadMapper especialidadMapper;
    private final AlmacenDeImagenes imagenes;
    private final EmailService emailService;
    private final Clock reloj;

    @Override
    public List<UserDto.Response> findAll() {
        return conPerfiles(usuarios.findAll());
    }

    @Override
    public List<UserDto.Response> findAllTatuadores() {
        return conPerfiles(usuarios.findAllByRol(TATUADOR));
    }

    @Override
    public UserDto.Response findById(Long id) {
        SweUsrapl usuario = activoOError(id);
        return usuarioMapper.toResponse(usuario, perfiles.findById(id).orElse(null));
    }

    @Override
    public UserDto.PerfilPublico findPerfilPublico(Long id) {
        SweUsrapl usuario = activoOError(id);
        // Solo los tatuadores tienen portfolio público; para el resto el perfil no existe.
        if (!usuario.hasRole(TATUADOR)) {
            throw new AppExceptions.ResourceNotFoundException("Tatuador", "id", id);
        }
        PerfilUsuario perfil = perfilDe(usuario);
        return new UserDto.PerfilPublico(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getProfilePhoto(),
                perfil.getEstudio(),
                aniosDesde(perfil.getFechaInicioActividad()),
                especialidadMapper.toResponseList(perfil.getEspecialidades()));
    }

    @Override
    @Transactional
    public AuthDto.TokenResponse register(UserDto.CreateRequest request) {
        recaptchaService.verifyIfEnabled(request.recaptchaToken());
        SweUsrDto.Response cuenta = sweUsrService.register(new SweUsrDto.CreateRequest(
                request.username(), request.email(), request.password(), request.realname(), request.surname(),
                request.birthDate()), CLIENTE);
        PerfilUsuario perfil = new PerfilUsuario(usuarios.getReferenceById(cuenta.id()));
        ubicar(perfil, request.localidad(), request.lat(), request.lng());
        perfiles.save(perfil);
        return authService.issueToken(cuenta.id());
    }

    @Override
    @Transactional
    public AuthDto.TokenResponse registerTatuador(UserDto.RegisterTatuadorRequest request) {
        recaptchaService.verifyIfEnabled(request.recaptchaToken());
        // Queda como Cliente hasta que un administrador apruebe la solicitud.
        SweUsrDto.Response cuenta = sweUsrService.register(new SweUsrDto.CreateRequest(
                request.username(), request.email(), request.password(), request.realname(), request.surname(),
                request.birthDate()), CLIENTE);
        SweUsrapl usuario = usuarios.getReferenceById(cuenta.id());

        PerfilUsuario perfil = new PerfilUsuario(usuario);
        perfil.setEstudio(request.estudio());
        ubicar(perfil, request.localidad(), request.lat(), request.lng());
        perfiles.save(perfil);

        SolicitudTatuador solicitud = new SolicitudTatuador();
        solicitud.setUser(usuario);
        solicitud.setEstudio(request.estudio());
        if (request.especialidadesIds() != null && !request.especialidadesIds().isEmpty()) {
            // List.toString() produce "[1, 2]", que es un arreglo JSON válido.
            solicitud.setEspecialidadesIds(request.especialidadesIds().toString());
        }
        solicitudes.save(solicitud);
        return authService.issueToken(cuenta.id());
    }

    @Override
    @Transactional
    public UserDto.Response update(Long id, UserDto.UpdateRequest request) {
        SweUsrapl usuario = activoOError(id);
        if (request.realname() != null) usuario.setRealname(request.realname());
        if (request.surname() != null) usuario.setSurname(request.surname());
        if (request.birthDate() != null) usuario.setBirthDate(request.birthDate());

        PerfilUsuario perfil = perfilDe(usuario);
        if (request.estudio() != null) perfil.setEstudio(request.estudio());
        if (request.fechaInicioActividad() != null) perfil.setFechaInicioActividad(request.fechaInicioActividad());
        if (request.instagramHandle() != null) perfil.setInstagramHandle(request.instagramHandle());
        ubicar(perfil, request.localidad(), request.lat(), request.lng());
        return usuarioMapper.toResponse(usuario, perfiles.save(perfil));
    }

    @Override
    @Transactional
    public UserDto.Response updateProfilePhoto(Long id, MultipartFile file) {
        SweUsrapl usuario = activoOError(id);
        if (tokensDeInstagram.findByTatuador_Id(id).isPresent()) {
            throw new AppExceptions.ForbiddenException(
                    "Tu foto de perfil está sincronizada con Instagram y no puede editarse manualmente.",
                    "FOTO_SINCRONIZADA_CON_INSTAGRAM");
        }
        usuario.setProfilePhoto(imagenes.subir(file, "profile-photos"));
        return usuarioMapper.toResponse(usuario, perfiles.findById(id).orElse(null));
    }

    @Override
    @Transactional
    public void updateRoles(Long id, UserDto.UpdateRolesRequest request) {
        sweUsrService.updateRoles(id, request.roleIds());
    }

    @Override
    public List<SweRolDto.Response> findAllRoles() {
        return sweUsrService.findAllRoles();
    }

    @Override
    public List<Long> findRoleIdsByUser(Long userId) {
        return sweUsrService.findRolesByUser(userId).stream().map(SweRolDto.Response::id).toList();
    }

    @Override
    public SweRolDto.Response findRoleById(Long roleId) {
        return roles.findById(roleId)
                .map(rol -> new SweRolDto.Response(rol.getId(), rol.getDescription(), rol.getStatus()))
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Rol", "id", roleId));
    }

    @Override
    public List<EspecialidadDto.Response> findEspecialidades(Long tatuadorId) {
        return especialidadMapper.toResponseList(perfilDe(activoOError(tatuadorId)).getEspecialidades());
    }

    @Override
    @Transactional
    public void assignEspecialidades(Long tatuadorId, List<Long> especialidadIds) {
        PerfilUsuario perfil = perfilDe(activoOError(tatuadorId));
        perfil.getEspecialidades().clear();
        perfil.getEspecialidades().addAll(especialidades.findAllById(especialidadIds));
        perfiles.save(perfil);
    }

    @Override
    @Transactional
    public void removeEspecialidad(Long tatuadorId, Long especialidadId) {
        PerfilUsuario perfil = perfilDe(activoOError(tatuadorId));
        perfil.getEspecialidades().removeIf(especialidad -> especialidad.getId().equals(especialidadId));
        perfiles.save(perfil);
    }

    @Override
    @Transactional
    public void forgotPassword(UserDto.ForgotPasswordRequest request) {
        sweUsrService.forgotPassword(request.email()).ifPresent(emailService::enviarRecuperacionDeContrasena);
    }

    @Override
    @Transactional
    public void resetPassword(UserDto.ResetPasswordRequest request) {
        sweUsrService.resetPassword(new SweUsrDto.ResetPasswordRequest(request.token(), request.newPassword()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sweUsrService.delete(id);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private List<UserDto.Response> conPerfiles(List<SweUsrapl> lista) {
        Map<Long, PerfilUsuario> perfilesDeLaLista = perfiles.porUsuario(lista.stream().map(SweUsrapl::getId).toList());
        return lista.stream().map(usuario -> usuarioMapper.toResponse(usuario, perfilesDeLaLista)).toList();
    }

    /** Los usuarios dados de baja se tratan como inexistentes. */
    private SweUsrapl activoOError(Long id) {
        return usuarios.findById(id)
                .filter(BaseEntity::isActive)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Usuario", "id", id));
    }

    /**
     * El perfil del usuario. Si no tiene (alta por fuera de la app) devuelve uno vacío sin guardar:
     * los métodos que modifican lo guardan con {@code perfiles.save}.
     */
    private PerfilUsuario perfilDe(SweUsrapl usuario) {
        return perfiles.findById(usuario.getId()).orElseGet(() -> new PerfilUsuario(usuario));
    }

    private static void ubicar(PerfilUsuario perfil, String localidad, Double lat, Double lng) {
        if (localidad != null) perfil.setLocalidad(localidad);
        if (lat != null) perfil.setLat(lat);
        if (lng != null) perfil.setLng(lng);
    }

    /** Años cumplidos desde el inicio de actividad; se publica así para no revelar la fecha. */
    private Integer aniosDesde(LocalDate desde) {
        return desde != null ? Period.between(desde, LocalDate.now(reloj)).getYears() : null;
    }
}
