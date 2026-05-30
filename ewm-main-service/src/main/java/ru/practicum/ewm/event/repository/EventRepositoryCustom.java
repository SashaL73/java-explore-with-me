package ru.practicum.ewm.event.repository;

import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.SortEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {

    List<Event> searchPublicEvents(String text,
                                   List<Long> categories,
                                   Boolean paid,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   Boolean onlyAvailable,
                                   SortEvent sort,
                                   Long from,
                                   Long size);

    List<Event> searchAdminEvents(List<Long> users,
                                  List<EventState> eventStates,
                                  List<Long> categories,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Long from,
                                  Long size);

    List<Event> searchEventsByOwner(Long initiatorId,
                                    Long from,
                                    Long size);

}
