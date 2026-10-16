package com.linked.job;

import com.dmc00.core.swe.evento.InicioDeSesion;
import com.linked.service.InstagramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cuándo se sincroniza Instagram, además de a pedido ({@code POST /api/instagram/sync}):
 * <ul>
 *   <li>cada {@code app.instagram.sincronizacion.cada} (2 h, como el scheduler de Node), todas las
 *       cuentas vinculadas;</li>
 *   <li>cuando un tatuador inicia sesión (evento {@link InicioDeSesion} de core-swe);</li>
 *   <li>cuando un tatuador termina de vincular su cuenta.</li>
 * </ul>
 * Los eventos se atienden en segundo plano ({@code @Async}): no demoran el login ni el callback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SincronizacionDeInstagram {

    private final InstagramService instagram;

    @Scheduled(fixedDelayString = "${app.instagram.sincronizacion.cada:PT2H}",
               initialDelayString = "${app.instagram.sincronizacion.demora-inicial:PT1M}")
    public void sincronizarTodas() {
        List<Long> tatuadores = instagram.tatuadoresVinculados();
        log.info("Sincronización programada de Instagram: {} cuentas vinculadas", tatuadores.size());
        tatuadores.forEach(this::mantener);
    }

    @Async
    @EventListener
    public void alIniciarSesion(InicioDeSesion sesion) {
        if (sesion.tieneRol("Tatuador")) {
            mantener(sesion.usuarioId());
        }
    }

    @Async
    @EventListener
    public void alVincularCuenta(InstagramService.CuentaVinculada vinculacion) {
        mantener(vinculacion.tatuadorId());
    }

    /** Un error de una cuenta no frena a las demás ni llega a quien disparó el evento. */
    private void mantener(Long tatuadorId) {
        try {
            instagram.mantener(tatuadorId);
        } catch (RuntimeException ex) {
            log.error("No se pudo sincronizar Instagram del tatuador {}: {}", tatuadorId, ex.getMessage());
        }
    }
}
