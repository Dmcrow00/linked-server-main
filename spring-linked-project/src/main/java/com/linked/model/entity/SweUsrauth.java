package com.linked.model.entity;

import com.core.model.entity.SweAuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "swe_usrauth",
       uniqueConstraints = @UniqueConstraint(name = "swe_usrauth_id_usrapl_key", columnNames = "id_usrapl"))
@Getter
@Setter
@NoArgsConstructor
public class SweUsrauth extends SweAuditableEntity {

    @NotBlank
    @Column(name = "password", nullable = false, length = 150)
    private String password;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_expires")
    private LocalDateTime resetPasswordExpires;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usrapl", nullable = false)
    private SweUsrapl usrapl;
}
