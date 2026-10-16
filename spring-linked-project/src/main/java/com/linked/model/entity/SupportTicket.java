package com.linked.model.entity;

import com.dmc00.core.data.BaseEntity;
import com.dmc00.core.swe.model.entity.SweUsrapl;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Ticket de soporte creado desde el formulario de ayuda del cliente web.
 * <p>
 * Tabla {@code hd_support_ticket}. Si quien lo crea tiene sesión iniciada, el usuario
 * queda vinculado al ticket mediante la tabla intermedia {@code hd_usr_st}.
 */
@Entity
@Table(name = "hd_support_ticket")
@Getter
@Setter
@NoArgsConstructor
public class SupportTicket extends BaseEntity {

    /**
     * Indica si el ticket sigue abierto. En la API se expone como {@code status};
     * no confundir con {@code status} de BaseEntity, que indica baja lógica.
     */
    @Column(name = "ticket_open", nullable = false)
    private boolean ticketOpen = true;

    @NotBlank
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Size(max = 50)
    @Column(name = "category", length = 50)
    private String category;

    @Size(max = 20)
    @Column(name = "priority", length = 20)
    private String priority;

    /** Página del cliente web desde la que se reportó el problema. */
    @Size(max = 500)
    @Column(name = "url_pagina", length = 500)
    private String urlPagina;

    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "screenshot", columnDefinition = "TEXT")
    private String screenshot;

    /** Respuesta del administrador o moderador. Al responder, el ticket se cierra. */
    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @Size(max = 254)
    @Column(name = "contact_email", length = 254)
    private String contactEmail;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "hd_usr_st",
               joinColumns = @JoinColumn(name = "id_ticket"),
               inverseJoinColumns = @JoinColumn(name = "id_user"))
    private List<SweUsrapl> usuarios = new ArrayList<>();
}
