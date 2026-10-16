package com.linked.service.impl;

import com.linked.dto.BusquedaDto;
import com.linked.mapper.EspecialidadMapper;
import com.linked.model.entity.PerfilUsuario;
import com.linked.repository.PerfilUsuarioRepository;
import com.linked.service.BusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implementación de {@link BusquedaService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusquedaServiceImpl implements BusquedaService {

    public static final double RADIO_KM_POR_DEFECTO = 50;
    private static final double RADIO_TIERRA_KM = 6371;

    private final PerfilUsuarioRepository perfiles;
    private final EspecialidadMapper especialidadMapper;

    @Override
    public List<BusquedaDto.TatuadorUbicacion> tatuadoresConUbicacion(boolean conSesion) {
        return buscar(null, null, 0, conSesion);
    }

    @Override
    public BusquedaDto.EstiloBusquedaResponse porEstilo(Double lat, Double lng, Double radioKm, boolean conSesion) {
        // TODO: derivar los estilos de las imágenes con IA y quedarse con los tatuadores cuya
        // especialidad coincida, ordenados por afinidad.
        double radio = radioKm != null ? radioKm : RADIO_KM_POR_DEFECTO;
        return new BusquedaDto.EstiloBusquedaResponse(List.of(), buscar(lat, lng, radio, conSesion));
    }

    /** Tatuadores con coordenadas; si se indica una zona, descarta los que quedan fuera. */
    private List<BusquedaDto.TatuadorUbicacion> buscar(Double lat, Double lng, double radioKm, boolean conSesion) {
        boolean filtrarPorZona = lat != null && lng != null && radioKm > 0;
        return perfiles.findAllTatuadoresConEspecialidades().stream()
                .filter(PerfilUsuario::tieneUbicacion)
                .filter(perfil -> !filtrarPorZona || distanciaKm(lat, lng, perfil.getLat(), perfil.getLng()) <= radioKm)
                .map(perfil -> new BusquedaDto.TatuadorUbicacion(
                        perfil.getId(),
                        perfil.getUsuario().getUsername(),
                        perfil.getUsuario().getProfilePhoto(),
                        perfil.getEstudio(),
                        perfil.getLocalidad(),
                        perfil.getLat(),
                        perfil.getLng(),
                        especialidadMapper.toResponseList(perfil.getEspecialidades()),
                        conSesion ? perfil.getInstagramHandle() : null))
                .toList();
    }

    /** Distancia entre dos coordenadas por la fórmula de Haversine, en kilómetros. */
    static double distanciaKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
