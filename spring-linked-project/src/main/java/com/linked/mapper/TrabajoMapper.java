package com.linked.mapper;

import com.linked.dto.TrabajoDto;
import com.linked.model.entity.Trabajo;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {SweUsrMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TrabajoMapper {

    @Mapping(target = "fotos", expression = "java(t.getFotos().stream().map(f -> f.getImagePath()).toList())")
    @Mapping(target = "tatuador", source = "tatuador")
    @Mapping(target = "likesCount", expression = "java(t.getFavoritos().size())")
    @Mapping(target = "likedByMe", constant = "false")
    @Mapping(target = "creationTimestamp",
             expression = "java(t.getCreationTimestamp() != null ? t.getCreationTimestamp().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)")
    TrabajoDto.Response toResponse(Trabajo t);

    List<TrabajoDto.Response> toResponseList(List<Trabajo> list);
}
