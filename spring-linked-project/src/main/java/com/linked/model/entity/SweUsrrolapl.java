package com.linked.model.entity;

import com.core.model.entity.SweAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "swe_usrrolapl",
       uniqueConstraints = @UniqueConstraint(
           name = "swe_usrrolapl_unique",
           columnNames = {"id_usrapl", "id_rolapl"}))
@Getter
@Setter
@NoArgsConstructor
public class SweUsrrolapl extends SweAuditableEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rolapl", nullable = false)
    private SweRolapl rolapl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usrapl", nullable = false)
    private SweUsrapl usrapl;
}
