package com.linked.model.entity;

import com.core.model.entity.SweAuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Perfil de usuario de la aplicación; es también el principal de Spring Security.
 * <p>
 * Tabla {@code swe_usrapl}. El login acepta username o email. Las credenciales están en
 * {@link SweUsrauth} y los roles en {@link SweUsrrolapl}. La baja es lógica: se completa
 * {@code deleteDate} y {@code status} pasa a {@code "false"}.
 */
@Entity
@Table(name = "swe_usrapl",
       indexes = {
           @Index(name = "idx_usrapl_location", columnList = "lat, lng")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "swe_usrapl_email_key",    columnNames = "email"),
           @UniqueConstraint(name = "swe_usrapl_username_key", columnNames = "username")
       })
@Getter
@Setter
@NoArgsConstructor
public class SweUsrapl extends SweAuditableEntity implements UserDetails {

    @Size(max = 25)
    @Column(name = "realname", length = 25)
    private String realname;

    @Size(max = 25)
    @Column(name = "surname", length = 25)
    private String surname;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 25)
    @Column(name = "username", nullable = false, length = 25)
    private String username;

    @Email(message = "Email invalido")
    @NotBlank(message = "El email es obligatorio")
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /** URL de la foto de perfil. */
    @Column(name = "profile_photo")
    private String profilePhoto;

    /** Nombre del estudio donde trabaja el tatuador. */
    @Size(max = 100)
    @Column(name = "estudio", length = 100)
    private String estudio;

    @Column(name = "fecha_inicio_actividad")
    private LocalDate fechaInicioActividad;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @Size(max = 255)
    @Column(name = "localidad", length = 255)
    private String localidad;

    /** Coordenadas del estudio o domicilio; se usan en la búsqueda de tatuadores por mapa. */
    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Size(max = 100)
    @Column(name = "instagram_handle", length = 100)
    private String instagramHandle;

    // ─── Relaciones swe_* ────────────────────────────────────────────────────

    @OneToOne(mappedBy = "usrapl", fetch = FetchType.LAZY,
              cascade = CascadeType.ALL, orphanRemoval = true)
    private SweUsrauth usrauth;

    @OneToMany(mappedBy = "usrapl", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SweUsrrolapl> userRoles = new ArrayList<>();

    // ─── Relación M:N con especialidades (tat_tatuador_especialidad) ─────────

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tat_tatuador_especialidad",
        joinColumns = @JoinColumn(name = "id_tatuador"),
        inverseJoinColumns = @JoinColumn(name = "id_especialidad")
    )
    private List<Especialidad> especialidades = new ArrayList<>();

    // ─── UserDetails ─────────────────────────────────────────────────────────

    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() {
        return usrauth != null ? usrauth.getPassword() : null;
    }

    /**
     * Roles vigentes con el prefijo {@code ROLE_} que espera {@code hasRole(...)}.
     * <p>
     * {@code userRoles} es diferida: quien cargue el usuario para autenticar debe traer los
     * roles en la misma consulta (ver {@code SweUsrRepository#findByUsernameForAuth}).
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (userRoles == null) return List.of();
        return userRoles.stream()
                .filter(ur -> "true".equals(ur.getStatus()))
                .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRolapl().getDescription()))
                .collect(Collectors.toList());
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    /** Un usuario dado de baja no puede iniciar sesión. */
    @Override
    public boolean isEnabled() {
        return "true".equals(getStatus()) && deleteDate == null;
    }

    // ─── Utilidades ──────────────────────────────────────────────────────────

    public String getFullName() {
        if (realname != null && surname != null) return realname + " " + surname;
        if (realname != null) return realname;
        return username;
    }

    /** Indica si el usuario tiene el rol vigente, sin distinguir mayúsculas. */
    public boolean hasRole(String rolDescription) {
        if (userRoles == null) return false;
        return userRoles.stream()
                .anyMatch(ur -> "true".equals(ur.getStatus())
                        && rolDescription.equalsIgnoreCase(ur.getRolapl().getDescription()));
    }

    public void softDelete() {
        this.deleteDate = LocalDateTime.now();
        this.disable();
    }
}
