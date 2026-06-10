package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.service.view.EventViews;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.participation.repository.projection.RequestsCountProjection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final EventViews eventViews;


    @Override
    public CompilationDto getCompilation(Long compId) {
        log.info("получение подборки событий id={}", compId);
        Compilation compilation = findCompilationWithEventsOrElseThrow(compId);
        return getCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Long from, Long size) {
        log.info("Получение подборок событий");
        List<Compilation> compilations = compilationRepository.searchCompilations(pinned, from, size);

        return compilations.stream()
                .map(this::getCompilationDto)
                .toList();
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Добавление подборки событий request={}", newCompilationDto);
        List<Event> events = new ArrayList<>();
        if (newCompilationDto.events() != null) {
            events = eventRepository.findAllById(newCompilationDto.events());
        }
        Compilation compilation = CompilationMapper.mapToCompilation(newCompilationDto, events);
        compilation = compilationRepository.save(compilation);
        return getCompilationDto(compilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки событий compId={}", compId);
        Compilation compilation = findCompilationOrElseThrow(compId);
        compilationRepository.delete(compilation);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Обновление подборки событий compId={}, request={}", compId, updateCompilationRequest);
        Compilation compilation = findCompilationWithEventsOrElseThrow(compId);
        List<Event> events;
        if (updateCompilationRequest.events() != null) {
            events = eventRepository.findAllById(updateCompilationRequest.events());
            if (!events.isEmpty()) {
                compilation.setEvents(events);
            }
        }

        if (updateCompilationRequest.pinned() != null) {
            compilation.setPinned(updateCompilationRequest.pinned());
        }

        if (updateCompilationRequest.title() != null) {
            compilation.setTitle(updateCompilationRequest.title());
        }
        compilationRepository.save(compilation);
        return getCompilationDto(compilation);
    }

    private Compilation findCompilationWithEventsOrElseThrow(Long compId) {
        return compilationRepository.findByIdWithEvents(compId).orElseThrow(
                () -> new NotFoundException("Подборка событий с id=" + compId + " не найдена")
        );
    }

    private Compilation findCompilationOrElseThrow(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка событий id=" + compId + " не найдена")
        );
    }

    private CompilationDto getCompilationDto(Compilation compilation) {
        List<Long> eventIds = compilation.getEvents().stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> requestsCountByIdEvent = Map.of();

        if (!eventIds.isEmpty()) {
            List<RequestsCountProjection> requestsCountProjections = participationRepository
                    .countByEventIdsAndStatus(eventIds, ParticipationStatus.CONFIRMED);

            requestsCountByIdEvent = requestsCountProjections.stream()
                    .collect(Collectors.toMap(
                            RequestsCountProjection::getEventId,
                            RequestsCountProjection::getCountRequests
                    ));
        }

        Map<Long, Long> finalRequestsCountByIdEvent = requestsCountByIdEvent;
        Map<Long, Long> viewById = eventViews.getView(eventIds);

        List<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                .map(event -> EventMapper.mapToEventShortDto(
                        event,
                        finalRequestsCountByIdEvent.getOrDefault(event.getId(), 0L),
                        viewById.getOrDefault(event.getId(), 0L)
                ))
                .toList();


        return CompilationMapper.mapToCompilationDto(compilation, eventShortDtos);
    }
}
