package com.linked.model.entity;

import com.core.model.entity.SweAuditableEntity;
import com.linked.model.enums.PermissionType;
import com.linked.model.enums.ResourceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "swe_accmodapl",
       uniqueConstraints = @UniqueConstraint(
           name = "swe_accmodapl_unique_permission",
           columnNames = {"id_rolapl", "resource_type", "resource_name", "project_name", "permission_type"}))
@Getter
@Setter
@NoArgsConstructor
public class SweAccmodapl extends SweAuditableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rolapl", nullable = false)
    private SweRolapl rolapl;

    @NotBlank
    @Size(max = 50)
    @Column(name = "project_name", nullable = false, length = 50)
    private String projectName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private ResourceType resourceType;

    @NotBlank
    @Size(max = 150)
    @Column(name = "resource_name", nullable = false, length = 150)
    private String resourceName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false, length = 20)
    private PermissionType permissionType;
}
