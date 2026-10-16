package com.linked.soporte;

import com.linked.integration.InstagramApi;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Instagram de mentira. Todo se configura por token de acceso, así cada test usa el suyo y no lo
 * pisa una sincronización en segundo plano de otro test. El código {@code X} del callback se canjea
 * por el token {@code token-X}.
 */
public class InstagramApiFalsa implements InstagramApi {

    public static final long SESENTA_DIAS = 60L * 24 * 60 * 60;

    private final Map<String, List<Publicacion>> publicaciones = new ConcurrentHashMap<>();
    private final Set<String> vencidos = ConcurrentHashMap.newKeySet();
    private final AtomicInteger renovaciones = new AtomicInteger();

    public void publicar(String accessToken, List<Publicacion> lista) {
        publicaciones.put(accessToken, List.copyOf(lista));
    }

    public void vencer(String accessToken) {
        vencidos.add(accessToken);
    }

    public int renovaciones() {
        return renovaciones.get();
    }

    @Override
    public String urlDeAutorizacion(String state) {
        return "https://instagram.test/oauth/authorize?state=" + state;
    }

    @Override
    public TokenDeAcceso canjearCodigo(String code) {
        return new TokenDeAcceso("token-" + code, SESENTA_DIAS, "ig-" + code);
    }

    @Override
    public TokenDeAcceso renovar(String accessToken) {
        if (vencidos.contains(accessToken)) {
            throw new TokenVencidoException("vencido");
        }
        renovaciones.incrementAndGet();
        String renovado = accessToken + "-renovado";
        publicaciones.computeIfPresent(accessToken, (token, lista) -> {
            publicaciones.put(renovado, lista);
            return lista;
        });
        return new TokenDeAcceso(renovado, SESENTA_DIAS, null);
    }

    @Override
    public Cuenta cuenta(String accessToken) {
        return new Cuenta("ig-cuenta", "tinta.fina", "https://instagram.test/foto-de-perfil.jpg");
    }

    @Override
    public List<Publicacion> publicaciones(String accessToken) {
        if (vencidos.contains(accessToken)) {
            throw new TokenVencidoException("vencido");
        }
        return publicaciones.getOrDefault(accessToken, List.of());
    }

    @Override
    public byte[] descargar(String url) {
        return url.getBytes(StandardCharsets.UTF_8);
    }
}
