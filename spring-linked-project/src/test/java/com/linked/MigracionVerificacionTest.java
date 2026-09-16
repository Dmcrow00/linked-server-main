package com.linked;

import com.linked.dto.HorarioHabitualDto;
import com.linked.dto.TurnoMensajeDto;
import com.linked.dto.TurnoSesionDto;
import com.linked.dto.UserDto;
import com.linked.exception.AppExceptions;
import com.linked.model.entity.SweRolapl;
import com.linked.model.entity.SweUsrapl;
import com.linked.model.entity.SweUsrrolapl;
import com.linked.repository.SweRolRepository;
import com.linked.repository.SweUsrRepository;
import com.linked.repository.SweUsrRolRepository;
import com.linked.repository.TurnoSesionRepository;
import com.linked.service.AgendaService;
import com.linked.service.TurnosService;
import com.linked.service.UserService;
import com.linked.service.impl.CloudinaryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica flujos del backend Spring que no se detectan compilando ni levantando el contexto:
 * carga diferida fuera de transacción, consultas con JOIN FETCH, reemplazo de colecciones con
 * restricciones únicas y reglas de negocio de turnos. Corre sobre H2 con el perfil {@code test}.
 */
@SpringBootTest
@ActiveProfiles("test")
class MigracionVerificacionTest {

    @Autowired private SweRolRepository rolRepository;
    @Autowired private SweUsrRepository usrRepository;
    @Autowired private SweUsrRolRepository usrRolRepository;
    @Autowired private TurnoSesionRepository turnoSesionRepository;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private AgendaService agendaService;
    @Autowired private TurnosService turnosService;
    @Autowired private UserService userService;

    // Reemplaza a Cloudinary sin Mockito: en JDK 25 Mockito no puede generar mocks de clases.
    @TestConfiguration
    static class CloudinaryFalso {
        @Bean
        @Primary
        CloudinaryServiceImpl cloudinaryServiceFalso() {
            return new CloudinaryServiceImpl(null) {
                @Override
                public String uploadImage(MultipartFile file, String folder) {
                    return "https://res.cloudinary.com/test/" + folder + "/" + file.getOriginalFilename();
                }
            };
        }
    }

    /**
     * Spring Security consulta los roles después de que {@code loadUserByUsername} cerró su
     * transacción, tanto en el login como en cada request autenticado con JWT.
     */
    @Test
    void losRolesDelUsuarioSeLeenFueraDeLaTransaccion() {
        SweUsrapl usuario = crearUsuario("auth_roles");
        asignarRol(usuario, rol("Tatuador"));

        UserDetails cargado = userDetailsService.loadUserByUsername("auth_roles");

        assertThat(cargado.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_Tatuador");
    }

    @Test
    void lasConsultasDeTurnosConDetalleSePuedenEjecutar() {
        assertThatCode(() -> turnoSesionRepository.findByClienteIdWithDetails(1L)).doesNotThrowAnyException();
        assertThatCode(() -> turnoSesionRepository.findByTatuadorIdWithDetails(1L)).doesNotThrowAnyException();
        assertThatCode(() -> turnoSesionRepository.findByIdWithDetails(1L)).doesNotThrowAnyException();
    }

    @Test
    void unClientePuedeSolicitarUnTurnoConImagenesYVerloEnSusReservas() {
        SweUsrapl cliente = crearUsuario("turno_cliente");
        SweUsrapl tatuador = crearUsuario("turno_tatuador");
        MultipartFile[] imagenes = {
                new MockMultipartFile("imagenes", "a.jpg", "image/jpeg", new byte[]{1}),
                new MockMultipartFile("imagenes", "b.jpg", "image/jpeg", new byte[]{2})
        };

        TurnoSesionDto.Response creado = turnosService.solicitarTurno(cliente.getId(),
                turno(tatuador, "2026-10-01T10:00:00", "2026-10-01T11:00:00"), imagenes);

        assertThat(creado.tatuadores()).hasSize(1);
        assertThat(creado.imagenesRef()).hasSize(2);

        List<TurnoSesionDto.Response> reservas = turnosService.getTurnosByCliente(cliente.getId());
        assertThat(reservas).hasSize(1);
        assertThat(reservas.get(0).tatuadores()).hasSize(1);
        assertThat(reservas.get(0).imagenesRef()).hasSize(2);
        assertThat(turnosService.getTurnosByTatuador(tatuador.getId())).hasSize(1);
    }

    @Test
    void noSePuedeReservarUnHorarioQueSeSuperponeConOtroTurnoDelTatuador() {
        SweUsrapl cliente = crearUsuario("solape_cliente");
        SweUsrapl tatuador = crearUsuario("solape_tatuador");
        turnosService.solicitarTurno(cliente.getId(),
                turno(tatuador, "2026-11-01T10:00:00", "2026-11-01T12:00:00"), null);

        assertThatThrownBy(() -> turnosService.solicitarTurno(cliente.getId(),
                turno(tatuador, "2026-11-01T11:00:00", "2026-11-01T13:00:00"), null))
                .isInstanceOf(AppExceptions.DuplicateResourceException.class);
    }

    @Test
    void soloLosParticipantesDelTurnoPuedenUsarSuChat() {
        SweUsrapl cliente = crearUsuario("chat_cliente");
        SweUsrapl tatuador = crearUsuario("chat_tatuador");
        SweUsrapl ajeno = crearUsuario("chat_ajeno");
        Long turnoId = turnosService.solicitarTurno(cliente.getId(),
                turno(tatuador, "2026-12-01T10:00:00", "2026-12-01T11:00:00"), null).id();

        turnosService.enviarMensaje(turnoId, cliente.getId(), new TurnoMensajeDto.CreateRequest("Hola"));

        assertThat(turnosService.getMensajes(turnoId, tatuador.getId())).hasSize(1);
        assertThatThrownBy(() -> turnosService.getMensajes(turnoId, ajeno.getId()))
                .isInstanceOf(AppExceptions.ForbiddenException.class);
        assertThatThrownBy(() -> turnosService.enviarMensaje(turnoId, ajeno.getId(),
                new TurnoMensajeDto.CreateRequest("Intruso")))
                .isInstanceOf(AppExceptions.ForbiddenException.class);
    }

    @Test
    void unTatuadorPuedeGuardarSuHorarioMasDeUnaVez() {
        SweUsrapl tatuador = crearUsuario("horario_tatuador");
        HorarioHabitualDto.SaveRequest horario = new HorarioHabitualDto.SaveRequest(
                List.of(new HorarioHabitualDto.DiaHorario(1, "10:00", "18:00", 60)));

        agendaService.saveHorarioHabitual(tatuador.getId(), horario);

        assertThatCode(() -> agendaService.saveHorarioHabitual(tatuador.getId(), horario))
                .doesNotThrowAnyException();
        assertThat(agendaService.getHorarioHabitual(tatuador.getId())).hasSize(1);
    }

    @Test
    void unAdministradorPuedeAgregarUnRolConservandoLosExistentes() {
        SweUsrapl usuario = crearUsuario("roles_usuario");
        Long cliente = rol("Cliente").getId();
        Long moderador = rol("Moderador").getId();

        userService.updateRoles(usuario.getId(), new UserDto.UpdateRolesRequest(List.of(cliente)));

        assertThatCode(() -> userService.updateRoles(usuario.getId(),
                new UserDto.UpdateRolesRequest(List.of(cliente, moderador))))
                .doesNotThrowAnyException();
        assertThat(usrRolRepository.findByUsrapl_Id(usuario.getId())).hasSize(2);
    }

    // BaseEntity inicializa version en 0, así que Spring Data trata las entidades nuevas como
    // existentes y hace merge: hay que usar siempre la instancia que devuelve save().

    private SweUsrapl crearUsuario(String username) {
        SweUsrapl usuario = new SweUsrapl();
        usuario.setUsername(username);
        usuario.setEmail(username + "@linked.test");
        usuario.setBirthDate(LocalDate.of(1990, 1, 1));
        return usrRepository.save(usuario);
    }

    private SweRolapl rol(String descripcion) {
        return rolRepository.findByDescription(descripcion).orElseGet(() -> {
            SweRolapl rol = new SweRolapl();
            rol.setDescription(descripcion);
            return rolRepository.save(rol);
        });
    }

    private void asignarRol(SweUsrapl usuario, SweRolapl rol) {
        SweUsrrolapl asignacion = new SweUsrrolapl();
        asignacion.setUsrapl(usuario);
        asignacion.setRolapl(rol);
        usrRolRepository.save(asignacion);
    }

    private TurnoSesionDto.CreateRequest turno(SweUsrapl tatuador, String inicio, String fin) {
        return new TurnoSesionDto.CreateRequest(tatuador.getId(), inicio, fin, null);
    }
}
