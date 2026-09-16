package com.linked.model.entity;

import com.core.model.entity.SweAuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Ítem del menú de navegación del cliente web.
 * <p>
 * Tabla {@code swe_itemmenu}. La estructura es jerárquica: un ítem puede colgar de otro
 * ({@code id_supitemmenu}) y sus hijos se ordenan por {@code order_number}.
 */
@Entity
@Table(name = "swe_itemmenu")
@Getter
@Setter
@NoArgsConstructor
public class SweItemmenu extends SweAuditableEntity {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 100)
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Size(max = 100)
    @Column(name = "icon", length = 100)
    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_supitemmenu")
    private SweItemmenu parentItem;

    @OneToMany(mappedBy = "parentItem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("orderNumber ASC NULLS LAST")
    private List<SweItemmenu> children = new ArrayList<>();

    /** Roles que pueden ver el ítem, separados por coma. Vacío = visible para todos. */
    @Column(name = "roles_permitidos")
    private String rolesPermitidos;

    /** Ruta del cliente web a la que navega el ítem. */
    @Size(max = 255)
    @Column(name = "route", length = 255)
    private String route;

    @Column(name = "order_number")
    private Integer orderNumber;

    /**
     * Indica si el ítem es visible para el rol dado según {@link #rolesPermitidos}.
     * La comparación ignora mayúsculas y espacios alrededor de cada rol.
     */
    public boolean isVisibleForRole(String rolDescription) {
        if (rolesPermitidos == null || rolesPermitidos.isBlank()) return true;
        for (String r : rolesPermitidos.split(",")) {
            if (r.trim().equalsIgnoreCase(rolDescription)) return true;
        }
        return false;
    }
}
