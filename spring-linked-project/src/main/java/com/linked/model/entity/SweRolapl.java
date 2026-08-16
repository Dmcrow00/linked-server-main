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
@Table(name = "swe_rolapl",
       uniqueConstraints = @UniqueConstraint(name = "swe_rolapl_description_key", columnNames = "description"))
@Getter
@Setter
@NoArgsConstructor
public class SweRolapl extends SweAuditableEntity {

    @NotBlank(message = "La descripcion del rol es obligatoria")
    @Size(max = 50, message = "La descripcion no puede superar los 50 caracteres")
    @Column(name = "description", nullable = false, length = 50)
    private String description;

    @OneToMany(mappedBy = "rolapl", fetch = FetchType.LAZY)
    private List<SweUsrrolapl> userRoles = new ArrayList<>();
}
