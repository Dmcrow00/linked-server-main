package com.linked.service.impl;

import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.swe.repository.SweUsrRepository;
import com.dmc00.core.web.AppExceptions;
import com.linked.dto.SupportTicketDto;
import com.linked.mapper.SupportTicketMapper;
import com.linked.model.entity.SupportTicket;
import com.linked.repository.SupportTicketRepository;
import com.linked.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Gestión de tickets de soporte: alta por usuarios y seguimiento por administradores y moderadores. */
@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final SweUsrRepository sweUsrRepository;
    private final SupportTicketMapper ticketMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto.Response> findAll() {
        return ticketMapper.toResponseList(ticketRepository.findAllByOrderByCreationTimestampDesc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto.Response> findByCurrentUser(String username) {
        return ticketMapper.toResponseList(ticketRepository.findByUsername(username));
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketDto.Response findById(Long id) {
        return ticketMapper.toResponse(getOrThrow(id));
    }

    /** @param username usuario autenticado, o {@code null} si el ticket se crea sin sesión */
    @Override
    @Transactional
    public SupportTicketDto.Response create(SupportTicketDto.CreateRequest request, String username) {
        SupportTicket ticket = new SupportTicket();
        ticket.setDescription(request.description());
        ticket.setCategory(request.category());
        ticket.setPriority(request.priority());
        ticket.setUrlPagina(request.urlPagina());
        ticket.setUserAgent(request.userAgent());
        ticket.setScreenshot(request.screenshot());
        ticket.setContactEmail(request.contactEmail());

        if (username != null) {
            sweUsrRepository.findByUsername(username).ifPresent(u -> ticket.getUsuarios().add(u));
        }

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public SupportTicketDto.Response update(Long id, SupportTicketDto.UpdateRequest request) {
        SupportTicket ticket = getOrThrow(id);
        if (request.status() != null) ticket.setTicketOpen(request.status());
        if (request.category() != null) ticket.setCategory(request.category());
        if (request.priority() != null) ticket.setPriority(request.priority());
        if (request.adminResponse() != null) ticket.setAdminResponse(request.adminResponse());
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    /** Registra la respuesta del administrador y cierra el ticket. */
    @Override
    @Transactional
    public void respond(Long id, String adminResponse) {
        SupportTicket ticket = getOrThrow(id);
        ticket.setAdminResponse(adminResponse);
        ticket.setTicketOpen(false);
        ticketRepository.save(ticket);
    }

    /** Borrado físico: a diferencia del resto de las entidades, los tickets no usan baja lógica. */
    @Override
    @Transactional
    public void delete(Long id) {
        ticketRepository.delete(getOrThrow(id));
    }

    private SupportTicket getOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Ticket", "id", id));
    }
}
