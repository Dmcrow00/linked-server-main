package com.linked.mapper;

import com.linked.dto.SupportTicketDto;
import com.linked.model.entity.SupportTicket;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupportTicketMapper {

    @Mapping(target = "status", expression = "java(st.isTicketOpen())")
    @Mapping(target = "usuarios",
             expression = "java(st.getUsuarios().stream().map(u -> u.getUsername()).toList())")
    @Mapping(target = "creationTimestamp",
             expression = "java(st.getCreationTimestamp() != null ? st.getCreationTimestamp().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)")
    @Mapping(target = "modificationTimestamp",
             expression = "java(st.getModificationTimestamp() != null ? st.getModificationTimestamp().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)")
    SupportTicketDto.Response toResponse(SupportTicket st);

    List<SupportTicketDto.Response> toResponseList(List<SupportTicket> list);
}
