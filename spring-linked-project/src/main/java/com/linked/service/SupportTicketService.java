package com.linked.service;

import com.linked.dto.SupportTicketDto;

import java.util.List;

public interface SupportTicketService {

    List<SupportTicketDto.Response> findAll();

    List<SupportTicketDto.Response> findByCurrentUser(String username);

    SupportTicketDto.Response findById(Long id);

    SupportTicketDto.Response create(SupportTicketDto.CreateRequest request, String username);

    SupportTicketDto.Response update(Long id, SupportTicketDto.UpdateRequest request);

    void respond(Long id, String adminResponse);

    void delete(Long id);
}
