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

    @Column(name = "roles_permitidos")
    private String rolesPermitidos;

    @Size(max = 255)
    @Column(name = "route", length = 255)
    private String route;

    @Column(name = "order_number")
    private Integer orderNumber;

    public boolean isVisibleForRole(String rolDescription) {
        if (rolesPermitidos == null || rolesPermitidos.isBlank()) return true;
        for (String r : rolesPermitidos.split(",")) {
            if (r.trim().equalsIgnoreCase(rolDescription)) return true;
        }
        return false;
    }
}
