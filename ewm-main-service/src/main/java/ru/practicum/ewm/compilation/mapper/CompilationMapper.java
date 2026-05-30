package ru.practicum.ewm.compilation.mapper;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;

import java.util.List;

public class CompilationMapper {
    public static CompilationDto mapToCompilationDto(Compilation compilation,
                                                     List<EventShortDto> eventShortDtos) {
        return CompilationDto.builder()
                .events(eventShortDtos)
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static Compilation mapToCompilation(NewCompilationDto compilationDto,
                                               List<Event> events) {
        return Compilation.builder()
                .title(compilationDto.title())
                .pinned(compilationDto.pinned() != null
                        ? compilationDto.pinned() : false)
                .events(events)
                .build();
    }


}
