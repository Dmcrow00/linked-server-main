package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hd_support_ticket")
@Getter
@Setter
@NoArgsConstructor
public class SupportTicket extends BaseEntity {

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

    @Size(max = 500)
    @Column(name = "url_pagina", length = 500)
    private String urlPagina;

    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "screenshot", columnDefinition = "TEXT")
    private String screenshot;

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
