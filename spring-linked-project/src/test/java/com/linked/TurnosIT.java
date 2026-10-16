package com.linked;

import com.dmc00.core.web.AppExceptions;
import com.linked.dto.HorarioHabitualDto;
import com.linked.dto.TurnoMensajeDto;
import com.linked.dto.TurnoSesionDto;
import com.linked.service.AgendaService;
import com.linked.service.TurnosService;
import com.linked.soporte.PruebaDeIntegracion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Turnos, agenda y chat. Cubre lo que no se ve compilando: consultas con JOIN FETCH, carga diferida,
 * reemplazo de colecciones con restricciones únicas y reglas de acceso.
 */
class TurnosIT extends PruebaDeIntegracion {

    @Autowired
    TurnosService turnos;

    @Autowired
    AgendaService agenda;

    @Test
    void unClienteSolicitaUnTurnoConImagenesYLoVenAmbos() {
        Long cliente = crearUsuario();
        Long tatuador = crearUsuario("Tatuador");
        MultipartFile[] imagenes = {
                new MockMultipartFile("imagenes", "a.jpg", "image/jpeg", new byte[] {1}),
                new MockMultipartFile("imagenes", "b.jpg", "image/jpeg", new byte[] {2})
        };

        TurnoSesionDto.Response creado = turnos.solicitarTurno(cliente,
                turno(tatuador, "2030-10-01T10:00:00", "2030-10-01T11:00:00"), imagenes);

        assertThat(creado.estado()).isEqualTo("Pendiente");
        assertThat(creado.imagenesRef()).hasSize(2);
        assertThat(creado.tatuadores()).extracting(asignado -> asignado.localidad()).containsExactly("Rosario");
        assertThat(turnos.getTurnosByCliente(cliente)).singleElement()
                .satisfies(reserva -> assertThat(reserva.imagenesRef()).hasSize(2));
        assertThat(turnos.getTurnosByTatuador(tatuador)).hasSize(1);
    }

    @Test
    void noSeReservaUnHorarioQueSeSuperponeConOtroTurnoDelTatuador() {
        Long cliente = crearUsuario();
        Long tatuador = crearUsuario("Tatuador");
        turnos.solicitarTurno(cliente, turno(tatuador, "2030-11-01T10:00:00", "2030-11-01T12:00:00"), null);

        assertThatThrownBy(() -> turnos.solicitarTurno(cliente,
                turno(tatuador, "2030-11-01T11:00:00", "2030-11-01T13:00:00"), null))
                .isInstanceOf(AppExceptions.ConflictException.class);
    }

    @Test
    void soloSeReservaConUnTatuadorYConFechasValidas() {
        Long cliente = crearUsuario();
        Long otroCliente = crearUsuario();
        Long tatuador = crearUsuario("Tatuador");

        assertThatThrownBy(() -> turnos.solicitarTurno(cliente,
                turno(otroCliente, "2030-11-02T10:00:00", "2030-11-02T11:00:00"), null))
                .isInstanceOf(AppExceptions.ResourceNotFoundException.class);
        assertThatThrownBy(() -> turnos.solicitarTurno(cliente,
                turno(tatuador, "2 de noviembre", "2030-11-02T11:00:00"), null))
                .isInstanceOf(AppExceptions.BadRequestException.class);
        assertThatThrownBy(() -> turnos.solicitarTurno(cliente,
                turno(tatuador, "2030-11-02T11:00:00", "2030-11-02T10:00:00"), null))
                .isInstanceOf(AppExceptions.BadRequestException.class);
    }

    @Test
    void soloLosParticipantesUsanElChatDelTurno() {
        Long cliente = crearUsuario();
        Long tatuador = crearUsuario("Tatuador");
        Long ajeno = crearUsuario();
        Long turnoId = turnos.solicitarTurno(cliente,
                turno(tatuador, "2030-12-01T10:00:00", "2030-12-01T11:00:00"), null).id();

        turnos.enviarMensaje(turnoId, cliente, new TurnoMensajeDto.CreateRequest("Hola"));

        assertThat(turnos.getMensajes(turnoId, tatuador)).singleElement()
                .satisfies(mensaje -> assertThat(mensaje.texto()).isEqualTo("Hola"));
        assertThatThrownBy(() -> turnos.getMensajes(turnoId, ajeno))
                .isInstanceOf(AppExceptions.ForbiddenException.class);
        assertThatThrownBy(() -> turnos.enviarMensaje(turnoId, ajeno, new TurnoMensajeDto.CreateRequest("Intruso")))
                .isInstanceOf(AppExceptions.ForbiddenException.class);
    }

    @Test
    void soloElTatuadorDelTurnoCambiaSuEstado() throws Exception {
        Long cliente = crearUsuario();
        Long tatuador = crearUsuario("Tatuador");
        Long otroTatuador = crearUsuario("Tatuador");
        Long turnoId = turnos.solicitarTurno(cliente,
                turno(tatuador, "2031-01-10T10:00:00", "2031-01-10T11:00:00"), null).id();

        mvc.perform(patch("/api/turnos/gestionar/" + turnoId).header(HttpHeaders.AUTHORIZATION, sesionDe(otroTatuador))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\": \"Confirmada\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/turnos/gestionar/" + turnoId).header(HttpHeaders.AUTHORIZATION, sesionDe(cliente))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\": \"Confirmada\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/turnos/gestionar/" + turnoId).header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\": \"Confirmada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("Confirmada"));
    }

    /** Como en Node: completado o cancelado, el turno ya no cambia de estado. */
    @Test
    void unTurnoCompletadoNoCambiaMas() throws Exception {
        Long cliente = crearUsuario();
        Long tatuador = crearUsuario("Tatuador");
        Long turnoId = turnos.solicitarTurno(cliente,
                turno(tatuador, "2031-02-10T10:00:00", "2031-02-10T11:00:00"), null).id();
        turnos.actualizarEstado(turnoId, new TurnoSesionDto.GestionarRequest("Completada"), tatuador);

        mvc.perform(patch("/api/turnos/gestionar/" + turnoId).header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\": \"Pendiente\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TURNO_CERRADO"));
        assertThatThrownBy(() -> turnos.actualizarEstado(turnoId, new TurnoSesionDto.GestionarRequest("Inventado"), tatuador))
                .isInstanceOf(AppExceptions.BadRequestException.class);
    }

    @Test
    void losHorariosReservadosNoAparecenEntreLosLibres() {
        Long cliente = crearUsuario();
        Long tatuador = crearUsuario("Tatuador");
        LocalDate lunes = LocalDate.of(2031, 3, 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        HorarioHabitualDto.SaveRequest horario = new HorarioHabitualDto.SaveRequest(
                List.of(new HorarioHabitualDto.DiaHorario(1, "10:00", "13:00", 60)));

        agenda.saveHorarioHabitual(tatuador, horario);
        // Guardarlo dos veces no choca con la restricción única (tatuador, día).
        assertThatCode(() -> agenda.saveHorarioHabitual(tatuador, horario)).doesNotThrowAnyException();
        turnos.solicitarTurno(cliente, turno(tatuador, lunes + "T11:00:00", lunes + "T12:00:00"), null);

        assertThat(agenda.getSlotsDisponibles(tatuador, lunes).slots()).containsExactly("10:00", "12:00");
        assertThat(agenda.getFechasBloqueadas(tatuador, lunes, lunes.plusDays(1)))
                .containsExactly(lunes.plusDays(1).toString());
    }

    private static TurnoSesionDto.CreateRequest turno(Long tatuador, String inicio, String fin) {
        return new TurnoSesionDto.CreateRequest(tatuador, inicio, fin, null);
    }
}
