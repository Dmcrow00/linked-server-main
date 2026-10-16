package com.linked.mapper;

import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.linked.dto.SupportTicketDto;
import com.linked.model.entity.SupportTicket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** Convierte tickets a DTO; {@code status} indica si el ticket está abierto ({@code ticketOpen}). */
@Mapper(componentModel = "spring", uses = FormatoDeFechas.class)
public interface SupportTicketMapper {

    @Mapping(target = "status", source = "ticketOpen")
    SupportTicketDto.Response toResponse(SupportTicket st);

    List<SupportTicketDto.Response> toResponseList(List<SupportTicket> list);

    default String username(SweUsrapl usuario) {
        return usuario.getUsername();
    }
}
