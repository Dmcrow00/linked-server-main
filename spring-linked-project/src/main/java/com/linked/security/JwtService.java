package com.linked.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Emisión y validación de los JWT de sesión.
 * <p>
 * Los tokens se firman con HMAC usando {@code app.jwt.secret} (codificado en Base64) y llevan
 * el username como subject. La vigencia se define en {@code app.jwt.expiration} (milisegundos).
 */
@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /** El state de OAuth solo tiene que sobrevivir al ida y vuelta con el proveedor. */
    private static final long OAUTH_STATE_EXPIRATION_MS = 10 * 60 * 1000L;

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /** Devuelve el username del token. Lanza {@link JwtException} si la firma no es válida. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Indica si el token tiene firma válida, no está vencido y pertenece al usuario dado. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException ex) {
            log.warn("Token JWT invalido: {}", ex.getMessage());
            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public long getExpiration() {
        return jwtExpiration;
    }

    /**
     * Genera el parámetro {@code state} de un flujo OAuth, firmado y con vencimiento corto.
     * <p>
     * Va y vuelve por el navegador del usuario, así que no puede ser el id en texto plano:
     * de lo contrario cualquiera podría invocar el callback y vincular su cuenta externa
     * al perfil de otro usuario.
     */
    public String generateOAuthState(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + OAUTH_STATE_EXPIRATION_MS))
                .signWith(getSignInKey())
                .compact();
    }

    /** @return id del usuario que inició el flujo, o {@code null} si el state no es válido o venció */
    public Long extractOAuthStateUserId(String state) {
        try {
            return Long.valueOf(extractAllClaims(state).getSubject());
        } catch (JwtException | NumberFormatException ex) {
            log.warn("State de OAuth invalido: {}", ex.getMessage());
            return null;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
