package com.linked.mapper;

import com.linked.dto.SweItemmenuDto;
import com.linked.model.entity.SweItemmenu;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SweItemmenuMapper {

    @Mapping(target = "idSupitemmenu",
             expression = "java(m.getParentItem() != null ? m.getParentItem().getId() : null)")
    SweItemmenuDto.Response toResponse(SweItemmenu m);

    List<SweItemmenuDto.Response> toResponseList(List<SweItemmenu> list);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationTimestamp", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationTimestamp", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "children", ignore = true)
    SweItemmenu toEntity(SweItemmenuDto.CreateRequest req);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationTimestamp", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationTimestamp", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "children", ignore = true)
    void updateEntity(SweItemmenuDto.UpdateRequest req, @MappingTarget SweItemmenu entity);
}
