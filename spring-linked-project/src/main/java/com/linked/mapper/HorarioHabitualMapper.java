package com.linked.mapper;

import com.linked.dto.HorarioHabitualDto;
import com.linked.model.entity.HorarioHabitual;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HorarioHabitualMapper {

    @Mapping(target = "horaInicio",
             expression = "java(h.getHoraInicio() != null ? h.getHoraInicio().toString() : null)")
    @Mapping(target = "horaFin",
             expression = "java(h.getHoraFin() != null ? h.getHoraFin().toString() : null)")
    HorarioHabitualDto.Response toResponse(HorarioHabitual h);

    List<HorarioHabitualDto.Response> toResponseList(List<HorarioHabitual> list);
}
