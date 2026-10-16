package com.linked.security;

import com.dmc00.core.security.JwtAuthenticationFilter;
import com.dmc00.core.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * El filtro JWT de core-security que, además del header {@code Authorization}, acepta el token
 * como parámetro {@code token} en una sola ruta: el inicio del OAuth de Instagram. Ahí el
 * navegador llega por una redirección ({@code window.location}), que no puede llevar headers.
 */
public class JwtConTokenEnInicioDeOAuth extends JwtAuthenticationFilter {

    static final String INICIO_DE_OAUTH = "/api/instagram/auth";

    public JwtConTokenEnInicioDeOAuth(JwtService jwtService, UserDetailsService userDetailsService) {
        super(jwtService, userDetailsService);
    }

    @Override
    protected String resolveToken(HttpServletRequest request) {
        String delHeader = super.resolveToken(request);
        if (delHeader != null) {
            return delHeader;
        }
        if ((request.getContextPath() + INICIO_DE_OAUTH).equals(request.getRequestURI())) {
            return request.getParameter("token");
        }
        return null;
    }
}
