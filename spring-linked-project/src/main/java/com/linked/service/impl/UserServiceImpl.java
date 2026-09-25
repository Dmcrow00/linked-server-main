package com.linked.service.impl;

import com.linked.dto.AuthDto;
import com.linked.dto.UserDto;
import com.linked.exception.AppExceptions;
import com.linked.mapper.SweUsrMapper;
import com.linked.model.entity.Especialidad;
import com.linked.model.entity.SweRolapl;
import com.linked.model.entity.SweUsrapl;
import com.linked.model.entity.SweUsrauth;
import com.linked.model.entity.SweUsrrolapl;
import com.linked.model.entity.SolicitudTatuador;
import com.linked.repository.EspecialidadRepository;
import com.linked.repository.SweRolRepository;
import com.linked.repository.SweUsrauthRepository;
import com.linked.repository.SweUsrRepository;
import com.linked.repository.SweUsrRolRepository;
import com.linked.repository.SolicitudTatuadorRepository;
import com.linked.security.JwtService;
import com.linked.security.RsaDecryptionService;
import com.linked.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de {@link UserService}.
 * <p>
 * Al crear entidades se usa siempre la instancia que devuelve {@code save()}: BaseEntity
 * inicializa {@code version} en 0, Spring Data las trata como existentes y hace {@code merge},
 * que devuelve una copia administrada distinta de la original.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SweUsrRepository sweUsrRepository;
    private final SweUsrauthRepository sweUsrauthRepository;
    private final SweUsrRolRepository sweUsrRolRepository;
    private final SweRolRepository sweRolRepository;
    private final EspecialidadRepository especialidadRepository;
    private final SolicitudTatuadorRepository solicitudRepository;
    private final SweUsrMapper sweUsrMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RsaDecryptionService rsaDecryptionService;
    private final CloudinaryServiceImpl cloudinaryService;
    private final EmailServiceImpl emailService;
    private final RecaptchaServiceImpl recaptchaService;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto.Response> findAll() {
        return sweUsrMapper.toResponseList(sweUsrRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto.Response> findAllTatuadores() {
        return sweUsrMapper.toResponseList(sweUsrRepository.findAllTatuadores());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto.Response findById(Long id) {
        return sweUsrMapper.toResponse(getActiveOrThrow(id));
    }

    @Override
    @Transactional
    public AuthDto.TokenResponse register(UserDto.CreateRequest request) {
        recaptchaService.verifyIfEnabled(request.recaptchaToken());
        validateNewUser(request.username(), request.email());

        SweUsrapl user = buildUser(request.username(), request.email(),
                request.realname(), request.surname(), request.birthDate(), null);
        if (request.localidad() != null) user.setLocalidad(request.localidad());
        if (request.lat() != null) user.setLat(request.lat());
        if (request.lng() != null) user.setLng(request.lng());
        user = sweUsrRepository.save(user);

        createAuth(user, request.password());
        assignRole(user, "Cliente");

        String token = jwtService.generateToken(user);
        return new AuthDto.TokenResponse(token, user.getUsername(), user.getId());
    }

    @Override
    @Transactional
    public AuthDto.TokenResponse registerTatuador(UserDto.RegisterTatuadorRequest request) {
        recaptchaService.verifyIfEnabled(request.recaptchaToken());
        validateNewUser(request.username(), request.email());

        SweUsrapl user = buildUser(request.username(), request.email(),
                request.realname(), request.surname(), request.birthDate(), request.estudio());
        if (request.localidad() != null) user.setLocalidad(request.localidad());
        if (request.lat() != null) user.setLat(request.lat());
        if (request.lng() != null) user.setLng(request.lng());
        user = sweUsrRepository.save(user);

        // Queda como Cliente hasta que un administrador apruebe la solicitud.
        createAuth(user, request.password());
        assignRole(user, "Cliente");

        SolicitudTatuador solicitud = new SolicitudTatuador();
        solicitud.setUser(user);
        solicitud.setEstudio(request.estudio());
        if (request.especialidadesIds() != null && !request.especialidadesIds().isEmpty()) {
            // List.toString() produce "[1, 2]", que es un arreglo JSON válido.
            solicitud.setEspecialidadesIds(request.especialidadesIds().toString());
        }
        solicitudRepository.save(solicitud);

        String token = jwtService.generateToken(user);
        return new AuthDto.TokenResponse(token, user.getUsername(), user.getId());
    }

    @Override
    @Transactional
    public UserDto.Response update(Long id, UserDto.UpdateRequest request) {
        SweUsrapl user = getActiveOrThrow(id);
        if (request.realname() != null) user.setRealname(request.realname());
        if (request.surname() != null) user.setSurname(request.surname());
        if (request.birthDate() != null) user.setBirthDate(request.birthDate());
        if (request.estudio() != null) user.setEstudio(request.estudio());
        if (request.fechaInicioActividad() != null) user.setFechaInicioActividad(request.fechaInicioActividad());
        if (request.localidad() != null) user.setLocalidad(request.localidad());
        if (request.lat() != null) user.setLat(request.lat());
        if (request.lng() != null) user.setLng(request.lng());
        if (request.instagramHandle() != null) user.setInstagramHandle(request.instagramHandle());
        return sweUsrMapper.toResponse(sweUsrRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto.Response updateProfilePhoto(Long id, MultipartFile file) {
        SweUsrapl user = getActiveOrThrow(id);
        String url = cloudinaryService.uploadImage(file, "profile-photos");
        user.setProfilePhoto(url);
        return sweUsrMapper.toResponse(sweUsrRepository.save(user));
    }

    @Override
    @Transactional
    public void updateRoles(Long id, UserDto.UpdateRolesRequest request) {
        getActiveOrThrow(id);
        sweUsrRolRepository.deleteByUsrapl_Id(id);
        // Hibernate ejecuta los INSERT antes que los DELETE pendientes: sin este flush, conservar
        // un rol que el usuario ya tenía viola la restricción única (usuario, rol).
        sweUsrRolRepository.flush();

        SweUsrapl userRef = sweUsrRepository.getReferenceById(id);
        for (Long roleId : request.roleIds()) {
            SweRolapl rol = sweRolRepository.findById(roleId)
                    .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Rol no encontrado: " + roleId));
            SweUsrrolapl ur = new SweUsrrolapl();
            ur.setUsrapl(userRef);
            ur.setRolapl(rol);
            sweUsrRolRepository.save(ur);
        }
    }

    @Override
    @Transactional
    public void assignEspecialidades(Long tatuadorId, List<Long> especialidadIds) {
        SweUsrapl user = getActiveOrThrow(tatuadorId);
        List<Especialidad> especialidades = especialidadRepository.findAllById(especialidadIds);
        user.getEspecialidades().clear();
        user.getEspecialidades().addAll(especialidades);
        sweUsrRepository.save(user);
    }

    @Override
    @Transactional
    public void removeEspecialidad(Long tatuadorId, Long especialidadId) {
        SweUsrapl user = getActiveOrThrow(tatuadorId);
        user.getEspecialidades().removeIf(e -> e.getId().equals(especialidadId));
        sweUsrRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(UserDto.ForgotPasswordRequest request) {
        // Si el email no existe no se informa nada, para no revelar qué cuentas están registradas.
        sweUsrRepository.findByEmail(request.email()).ifPresent(user -> {
            SweUsrauth auth = sweUsrauthRepository.findByUsrapl_Id(user.getId())
                    .orElseThrow(() -> new AppExceptions.InternalException("Auth no encontrado para el usuario"));
            String token = UUID.randomUUID().toString();
            auth.setResetPasswordToken(token);
            auth.setResetPasswordExpires(LocalDateTime.now().plusHours(1));
            sweUsrauthRepository.save(auth);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);
        });
    }

    @Override
    @Transactional
    public void resetPassword(UserDto.ResetPasswordRequest request) {
        SweUsrauth auth = sweUsrauthRepository.findByResetPasswordToken(request.token())
                .orElseThrow(() -> new AppExceptions.BadRequestException("Token inválido o expirado"));

        if (auth.getResetPasswordExpires() == null ||
            auth.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
            throw new AppExceptions.BadRequestException("Token expirado");
        }

        auth.setPassword(passwordEncoder.encode(rsaDecryptionService.decrypt(request.newPassword())));
        auth.setResetPasswordToken(null);
        auth.setResetPasswordExpires(null);
        sweUsrauthRepository.save(auth);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SweUsrapl user = getActiveOrThrow(id);
        user.setDeleteDate(LocalDateTime.now());
        user.disable();
        sweUsrRepository.save(user);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Los usuarios dados de baja se tratan como inexistentes. */
    private SweUsrapl getActiveOrThrow(Long id) {
        SweUsrapl u = sweUsrRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Usuario no encontrado: " + id));
        if (!"true".equals(u.getStatus())) {
            throw new AppExceptions.ResourceNotFoundException("Usuario no encontrado: " + id);
        }
        return u;
    }

    private void validateNewUser(String username, String email) {
        if (sweUsrRepository.existsByUsername(username)) {
            throw new AppExceptions.DuplicateResourceException("El nombre de usuario ya está en uso");
        }
        if (sweUsrRepository.existsByEmail(email)) {
            throw new AppExceptions.DuplicateResourceException("El email ya está registrado");
        }
    }

    private SweUsrapl buildUser(String username, String email, String realname,
                                 String surname, LocalDate birthDate, String estudio) {
        SweUsrapl user = new SweUsrapl();
        user.setUsername(username);
        user.setEmail(email);
        user.setRealname(realname);
        user.setSurname(surname);
        user.setBirthDate(birthDate);
        user.setEstudio(estudio);
        return user;
    }

    /** Descifra la contraseña recibida y guarda su hash BCrypt. */
    private void createAuth(SweUsrapl user, String encryptedPassword) {
        String plainPassword = rsaDecryptionService.decrypt(encryptedPassword);
        SweUsrauth auth = new SweUsrauth();
        auth.setUsrapl(user);
        auth.setPassword(passwordEncoder.encode(plainPassword));
        sweUsrauthRepository.save(auth);
    }

    private void assignRole(SweUsrapl user, String rolDescription) {
        SweRolapl rol = sweRolRepository.findByDescription(rolDescription)
                .orElseThrow(() -> new AppExceptions.InternalException("Rol '" + rolDescription + "' no encontrado"));
        SweUsrrolapl ur = new SweUsrrolapl();
        ur.setUsrapl(user);
        ur.setRolapl(rol);
        sweUsrRolRepository.save(ur);
    }
}
