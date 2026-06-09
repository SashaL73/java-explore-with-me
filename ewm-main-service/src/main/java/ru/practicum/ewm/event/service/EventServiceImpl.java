package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.exception.BadRequestException;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.SortEvent;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.service.view.EventViews;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.participation.repository.projection.RequestsCountProjection;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.svc.EndpointHit;
import ru.practicum.svc.StatsClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private final EventViews eventViews;

    @Override
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         SortEvent sort,
                                         Long from,
                                         Long size,
                                         HttpServletRequest request) {

        log.info("Публичное получение списка событий");

        if (rangeEnd != null && rangeStart != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new BadRequestException("Дата начала позже даты конца");
            }
        }

        List<Event> events = eventRepository.searchPublicEvents(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size
        );

        saveHit(request.getRequestURI(), request.getRemoteAddr());

        List<EventShortDto> eventShortDtos = getEventShortDtos(events, ParticipationStatus.CONFIRMED);

        if (SortEvent.VIEWS.equals(sort)) {
            return eventShortDtos.stream()
                    .sorted(Comparator.comparing(EventShortDto::views).reversed())
                    .toList();
        }

        return eventShortDtos;
    }

    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users,
                                             List<EventState> eventStates,
                                             List<Long> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             Long from,
                                             Long size) {
        log.info("Получение списка событий администратором по фильтрам");
        List<Event> events = eventRepository.searchAdminEvents(users, eventStates, categories, rangeStart, rangeEnd,
                from, size);

        if (events == null || events.isEmpty()) {
            return List.of();
        }

        List<Long> eventsId = events.stream().map(Event::getId).toList();
        Map<Long, Long> requestsMapCount = requestsCountByIdEvent(eventsId, ParticipationStatus.CONFIRMED);
        Map<Long, Long> viewById = eventViews.getView(eventsId);

        return events.stream()
                .map(event -> {
                    return EventMapper.mapToEventFullDto(event,
                            CategoryMapper.mapToCategoryDto(event.getCategory()),
                            requestsMapCount.getOrDefault(event.getId(), 0L),
                            UserMapper.mapToUserShortDto(event.getInitiator()),
                            EventMapper.mapToLocationDto(event.getLocation()),
                            viewById.getOrDefault(event.getId(), 0L));

                })
                .toList();


    }

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        log.info("Получение события id={}", id);
        Event event = eventRepository.findByIdAndEventStateWithCategoryAndInitiator(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));

        LocationDto locationDto = EventMapper.mapToLocationDto(event.getLocation());
        CategoryDto categoryDto = CategoryMapper.mapToCategoryDto(event.getCategory());
        UserShortDto userShortDto = UserMapper.mapToUserShortDto(event.getInitiator());

        Map<Long, Long> requestsMapCount = requestsCountByIdEvent(List.of(event.getId()), ParticipationStatus.CONFIRMED);
        saveHit(request.getRequestURI(), request.getRemoteAddr());

        Map<Long, Long> viewById = eventViews.getView(List.of(event.getId()));
        return EventMapper.mapToEventFullDto(event,
                categoryDto,
                requestsMapCount.get(event.getId()),
                userShortDto,
                locationDto,
                viewById.getOrDefault(event.getId(), 0L));
    }

    @Override
    public List<EventShortDto> getEventsByIdInitiator(Long id, Long from, Long size) {
        log.info("Получение списка событий инициатора id={}", id);
        List<Event> events = eventRepository.searchEventsByOwner(id, from, size);
        if (events == null) {
            return List.of();
        }

        return getEventShortDtos(events, ParticipationStatus.CONFIRMED);
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long initiatorId, NewEventDto newEventDto) {
        log.info("Создание события пользователем id={}, event={}", initiatorId, newEventDto);
        User user = findUserOrElseThrow(initiatorId);
        if (newEventDto.eventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("дата и время на которые намечено событие не может быть раньше," +
                    " чем через два часа от текущего момента");
        }
        Category category = findCategoryOrElseThrow(newEventDto.category());
        Location location = EventMapper.mapToLocation(newEventDto.location());

        Event event = EventMapper.mapToEvent(newEventDto, category, user, location);

        Event savedEvent = eventRepository.save(event);

        LocationDto locationDto = EventMapper.mapToLocationDto(location);
        CategoryDto categoryDto = CategoryMapper.mapToCategoryDto(category);
        UserShortDto userShortDto = UserMapper.mapToUserShortDto(user);

        return EventMapper.mapToEventFullDto(savedEvent,
                categoryDto,
                0L,
                userShortDto,
                locationDto,
                0L);
    }

    @Override
    public EventFullDto getEventByOwner(Long userId, Long eventId) {
        log.info("Получение события инициатором userId={}, eventId={}", userId, eventId);

        findUserOrElseThrow(userId);

        Event event = eventRepository.findByIdAndEventInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие c id= " + eventId + "не найдено"));

        LocationDto locationDto = EventMapper.mapToLocationDto(event.getLocation());
        CategoryDto categoryDto = CategoryMapper.mapToCategoryDto(event.getCategory());
        UserShortDto userShortDto = UserMapper.mapToUserShortDto(event.getInitiator());

        Map<Long, Long> requestsMapCount = requestsCountByIdEvent(List.of(event.getId()), ParticipationStatus.CONFIRMED);
        Map<Long, Long> viewById = eventViews.getView(List.of(event.getId()));

        return EventMapper.mapToEventFullDto(event,
                categoryDto,
                requestsMapCount.get(event.getId()),
                userShortDto,
                locationDto,
                viewById.getOrDefault(event.getId(), 0L));

    }

    @Override
    @Transactional
    public EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Обновление события eventId={} пользователем userId={} request={}", eventId, userId, request);
        findUserOrElseThrow(userId);

        Event event = eventRepository.findByIdAndEventInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие c id=" + eventId + "не найдено"));

        if (event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Только ожидающие события могут быть опубликованы");
        }

        if (request.eventDate() != null &&
                request.eventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("дата и время на которые намечено событие не может быть раньше," +
                    " чем через два часа от текущего момента");
        }

        if (request.category() != null) {
            Category category = findCategoryOrElseThrow(request.category());
            event.setCategory(category);
        }

        EventMapper.updateEventUserRequest(event, request);
        Event updatedEvent = eventRepository.save(event);

        LocationDto locationDto = EventMapper.mapToLocationDto(updatedEvent.getLocation());
        CategoryDto categoryDto = CategoryMapper.mapToCategoryDto(updatedEvent.getCategory());
        UserShortDto userShortDto = UserMapper.mapToUserShortDto(updatedEvent.getInitiator());

        Map<Long, Long> requestsMapCount = requestsCountByIdEvent(List.of(updatedEvent.getId()), ParticipationStatus.CONFIRMED);
        Map<Long, Long> viewById = eventViews.getView(List.of(event.getId()));

        return EventMapper.mapToEventFullDto(updatedEvent,
                categoryDto,
                requestsMapCount.get(updatedEvent.getId()),
                userShortDto,
                locationDto,
                viewById.getOrDefault(event.getId(), 0L));
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        log.info("Обновление события администратором eventId={} request={}", eventId, request);
        Event event = eventRepository.findEventByIdWithCategoryAndInitiator(eventId);

        if (request.eventDate() != null && request.eventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException("Дата события должна быть не позднее чем через 1 час после публикации");
        }

        if (request.category() != null) {
            Category category = findCategoryOrElseThrow(request.category());
            event.setCategory(category);
        }

        if (request.stateAction() != null) {
            switch (request.stateAction()) {
                case PUBLISH_EVENT -> publishEvent(event);
                case REJECT_EVENT -> rejectEvent(event);
            }
        }

        EventMapper.updateEventAdminRequest(event, request);
        Event updatedEvent = eventRepository.save(event);

        LocationDto locationDto = EventMapper.mapToLocationDto(updatedEvent.getLocation());
        CategoryDto categoryDto = CategoryMapper.mapToCategoryDto(updatedEvent.getCategory());
        UserShortDto userShortDto = UserMapper.mapToUserShortDto(updatedEvent.getInitiator());

        Map<Long, Long> requestsMapCount = requestsCountByIdEvent(List.of(updatedEvent.getId()), ParticipationStatus.CONFIRMED);
        Map<Long, Long> viewById = eventViews.getView(List.of(event.getId()));

        return EventMapper.mapToEventFullDto(updatedEvent,
                categoryDto,
                requestsMapCount.get(updatedEvent.getId()),
                userShortDto,
                locationDto,
                viewById.getOrDefault(event.getId(), 0L));

    }

    private List<EventShortDto> getEventShortDtos(List<Event> events, ParticipationStatus status) {
        List<Long> eventsIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> requestsMapCount = requestsCountByIdEvent(eventsIds, status);
        Map<Long, Long> eventViewById = eventViews.getView(eventsIds);

        return events.stream()
                .map(event -> EventMapper.mapToEventShortDto(
                        event,
                        requestsMapCount.getOrDefault(event.getId(), 0L),
                        eventViewById.getOrDefault(event.getId(), 0L)
                ))
                .toList();

    }

    private Map<Long, Long> requestsCountByIdEvent(List<Long> eventsIds, ParticipationStatus status) {
        List<RequestsCountProjection> requestsCount = participationRepository
                .countByEventIdsAndStatus(eventsIds, status);

        return requestsCount.stream()
                .collect(Collectors.toMap(RequestsCountProjection::getEventId, RequestsCountProjection::getCountRequests));

    }

    private User findUserOrElseThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Category findCategoryOrElseThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));
    }

    private void publishEvent(Event event) {
        if (!event.getEventState().equals(EventState.PENDING)) {
            throw new ConflictException("Только ожидающие события события могут быть опубликованы");
        }

        event.setEventState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void rejectEvent(Event event) {
        if (event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Опубликованное событие не может быть отменено");
        }

        event.setEventState(EventState.CANCELED);
    }

    private void saveHit(String uri, String ip) {
        EndpointHit hit = new EndpointHit(
                "ewm-main-service",
                uri,
                ip,
                LocalDateTime.now()
        );

        statsClient.saveHit(hit);

    }

}
