package com.linked.model.entity;

import com.dmc00.core.swe.model.entity.SweUsrapl;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lo que linked guarda de un usuario además de su cuenta: estudio, inicio de actividad,
 * ubicación, Instagram y especialidades.
 * <p>
 * Tabla {@code tat_perfil_usuario}, con el mismo id que el usuario de {@code swe_usrapl}
 * (core-swe solo mapea los datos comunes a todas las aplicaciones: ADR-0004 de dmc00-core).
 * Todo usuario tiene su perfil: lo crean V19 para los existentes y el registro para los nuevos.
 * <p>
 * Las entidades que muestran datos de un tatuador o cliente (trabajos, turnos, solicitudes)
 * apuntan al perfil; las que solo necesitan el id o el username, al usuario.
 */
@Entity
@Table(name = "tat_perfil_usuario")
@Getter
@Setter
@NoArgsConstructor
public class PerfilUsuario {

    @Id
    @Column(name = "id_usrapl")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usrapl")
    private SweUsrapl usuario;

    /** Nombre del estudio donde trabaja el tatuador. */
    @Size(max = 100)
    @Column(name = "estudio", length = 100)
    private String estudio;

    @Column(name = "fecha_inicio_actividad")
    private LocalDate fechaInicioActividad;

    @Size(max = 255)
    @Column(name = "localidad", length = 255)
    private String localidad;

    /** Coordenadas del estudio o domicilio; las usa la búsqueda de tatuadores por mapa. */
    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    /** Usuario de Instagram; lo completa la vinculación de la cuenta. */
    @Size(max = 100)
    @Column(name = "instagram_handle", length = 100)
    private String instagramHandle;

    /** Tabla {@code tat_tatuador_especialidad}: su columna {@code id_tatuador} es el id del usuario. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tat_tatuador_especialidad",
            joinColumns = @JoinColumn(name = "id_tatuador"),
            inverseJoinColumns = @JoinColumn(name = "id_especialidad"))
    private List<Especialidad> especialidades = new ArrayList<>();

    public PerfilUsuario(SweUsrapl usuario) {
        this.usuario = usuario;
    }

    public boolean tieneUbicacion() {
        return lat != null && lng != null;
    }
}
