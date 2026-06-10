package ru.practicum.ewm.compilation.dto;

import lombok.Builder;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

@Builder
public record CompilationDto(
        List<EventShortDto> events,
        Long id,
        Boolean pinned,
        String title
) {
}
