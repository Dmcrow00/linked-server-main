package com.linked.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class BusquedaServiceImplTest {

    @Test
    void laDistanciaEntreRosarioYCordobaRondaLos375Km() {
        double km = BusquedaServiceImpl.distanciaKm(-32.95, -60.65, -31.42, -64.18);

        assertThat(km).isCloseTo(375, within(15.0));
    }

    @Test
    void unPuntoEstaACeroKmDeSiMismo() {
        assertThat(BusquedaServiceImpl.distanciaKm(-34.6, -58.4, -34.6, -58.4)).isZero();
    }
}
