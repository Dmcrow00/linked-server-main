package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import com.linked.model.enums.SolicitudStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tat_solicitud_tatuador")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudTatuador extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private SweUsrapl user;

    @Size(max = 100)
    @Column(name = "estudio", length = 100)
    private String estudio;

    @Column(name = "especialidades_ids", columnDefinition = "jsonb")
    private String especialidadesIds;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "solicitud_status", nullable = false, length = 20)
    private SolicitudStatus solicitudStatus = SolicitudStatus.pendiente;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Size(max = 100)
    @Column(name = "admin_user", length = 100)
    private String adminUser;
}
