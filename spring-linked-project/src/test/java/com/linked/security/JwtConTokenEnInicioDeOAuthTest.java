package com.linked.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class JwtConTokenEnInicioDeOAuthTest {

    private final JwtConTokenEnInicioDeOAuth filtro = new JwtConTokenEnInicioDeOAuth(null, null);

    @Test
    void enElInicioDelOAuthTomaElTokenDelParametro() {
        MockHttpServletRequest pedido = new MockHttpServletRequest("GET", "/api/instagram/auth");
        pedido.setParameter("token", "jwt-por-query");

        assertThat(filtro.resolveToken(pedido)).isEqualTo("jwt-por-query");
    }

    @Test
    void elHeaderTienePrioridad() {
        MockHttpServletRequest pedido = new MockHttpServletRequest("GET", "/api/instagram/auth");
        pedido.addHeader("Authorization", "Bearer jwt-del-header");
        pedido.setParameter("token", "jwt-por-query");

        assertThat(filtro.resolveToken(pedido)).isEqualTo("jwt-del-header");
    }

    @Test
    void enCualquierOtraRutaIgnoraElParametro() {
        MockHttpServletRequest pedido = new MockHttpServletRequest("GET", "/api/users/1");
        pedido.setParameter("token", "jwt-por-query");

        assertThat(filtro.resolveToken(pedido)).isNull();
    }
}
