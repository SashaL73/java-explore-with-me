package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.SortEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getEvents(String text,
                                  List<Long> categories,
                                  Boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Boolean onlyAvailable,
                                  SortEvent sort,
                                  Long from,
                                  Long size,
                                  HttpServletRequest request);

    List<EventFullDto> getEventsAdmin(List<Long> users,
                                      List<EventState> eventStates,
                                      List<Long> categories,
                                      LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd,
                                      Long from,
                                      Long size);

    EventFullDto getEventById(Long id, HttpServletRequest request);

    List<EventShortDto> getEventsByIdInitiator(Long id, Long from, Long size);

    EventFullDto createEvent(Long initiatorId, NewEventDto newEventDto);

    EventFullDto getEventByOwner(Long userId, Long eventId);

    EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequest request);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getSubscriptionEvents(Long subscriberId);

}
