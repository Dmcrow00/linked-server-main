package com.linked.security;

/**
 * Expresiones de {@code @PreAuthorize} de los controllers. Todo endpoint declara la suya (lo
 * verifica {@code ArquitecturaTest}); {@code SecurityConfig} solo distingue público de autenticado.
 * <p>
 * Los nombres de rol son los de {@code swe_rolapl.description}, con la misma mayúscula.
 */
public final class Permisos {

    public static final String PUBLICO = "permitAll()";
    public static final String AUTENTICADO = "isAuthenticated()";
    public static final String ADMINISTRADOR = "hasRole('Administrador')";
    public static final String ADMINISTRADOR_O_MODERADOR = "hasAnyRole('Administrador','Moderador')";
    public static final String TATUADOR = "hasRole('Tatuador')";
    public static final String CLIENTE = "hasRole('Cliente')";

    /** El usuario del path ({@code {id}}) es el de la sesión, o la sesión es de un administrador. */
    public static final String EL_MISMO_O_ADMINISTRADOR =
            "hasRole('Administrador') or #id == authentication.principal.id";

    private Permisos() {
    }
}
