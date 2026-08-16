package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tat_trabajo")
@Getter
@Setter
@NoArgsConstructor
public class Trabajo extends BaseEntity {

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tatuador", nullable = false)
    private SweUsrapl tatuador;

    @Size(max = 100)
    @Column(name = "instagram_media_id", length = 100)
    private String instagramMediaId;

    @OneToMany(mappedBy = "trabajo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TrabajoFoto> fotos = new ArrayList<>();

    @OneToMany(mappedBy = "trabajo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TrabajoFavorito> favoritos = new ArrayList<>();
}
