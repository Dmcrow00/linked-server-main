package com.linked.dto;

import java.util.List;

/**
 * Horarios libres de un tatuador en un día.
 *
 * @param slots horas de inicio disponibles, en formato {@code HH:mm}
 */
public record SlotDto(List<String> slots) {}
