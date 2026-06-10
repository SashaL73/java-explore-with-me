package ru.practicum.ewm.event.mapper;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

public class EventMapper {
    public static EventShortDto mapToEventShortDto(Event event,
                                                   Long confirmedRequests,
                                                   Long views) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapToCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(UserMapper.mapToUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static EventFullDto mapToEventFullDto(Event event,
                                                 CategoryDto categoryDto,
                                                 Long confirmedRequests,
                                                 UserShortDto initiator,
                                                 LocationDto location,
                                                 Long views) {

        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(categoryDto)
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(initiator)
                .location(location)
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getEventState())
                .title(event.getTitle())
                .views(views)
                .build();

    }

    public static Event mapToEvent(NewEventDto newEventDto,
                                   Category category,
                                   User initiator,
                                   Location location) {
        return Event.builder()
                .createdOn(LocalDateTime.now())
                .annotation(newEventDto.annotation())
                .category(category)
                .description(newEventDto.description())
                .eventDate(newEventDto.eventDate())
                .initiator(initiator)
                .location(location)
                .paid(newEventDto.paid() != null
                        ? newEventDto.paid() : false)
                .participantLimit(newEventDto.participantLimit() != null
                        ? newEventDto.participantLimit() : 0L)
                .publishedOn(null)
                .requestModeration(newEventDto.requestModeration() != null
                        ? newEventDto.requestModeration() : true)
                .eventState(EventState.PENDING)
                .title(newEventDto.title())
                .build();
    }

    public static LocationDto mapToLocationDto(Location location) {
        return LocationDto.builder()
                .lon(location.getLon())
                .lat(location.getLat())
                .build();
    }

    public static Location mapToLocation(LocationDto locationDto) {
        return Location.builder()
                .lon(locationDto.lon())
                .lat(locationDto.lat())
                .build();
    }

    public static void updateEventUserRequest(Event event, UpdateEventUserRequest request) {
        if (request.annotation() != null && !request.annotation().isBlank()) {
            event.setAnnotation(request.annotation());
        }

        if (request.description() != null && !request.description().isBlank()) {
            event.setDescription(request.description());
        }

        if (request.eventDate() != null) {
            event.setEventDate(request.eventDate());
        }

        if (request.location() != null) {
            Location location = EventMapper.mapToLocation(request.location());
            event.setLocation(location);
        }

        if (request.paid() != null) {
            event.setPaid(request.paid());
        }

        if (request.participantLimit() != null) {
            event.setParticipantLimit(request.participantLimit());
        }

        if (request.requestModeration() != null) {
            event.setRequestModeration(request.requestModeration());
        }

        if (request.stateAction() != null) {

            switch (request.stateAction()) {

                case SEND_TO_REVIEW -> event.setEventState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setEventState(EventState.CANCELED);

            }
        }

        if (request.title() != null && !request.title().isBlank()) {
            event.setTitle(request.title());
        }

    }

    public static void updateEventAdminRequest(Event event, UpdateEventAdminRequest request) {
        if (request.annotation() != null && !request.annotation().isBlank()) {
            event.setAnnotation(request.annotation());
        }

        if (request.description() != null && !request.description().isBlank()) {
            event.setDescription(request.description());
        }

        if (request.eventDate() != null) {
            event.setEventDate(request.eventDate());
        }

        if (request.location() != null) {
            Location location = EventMapper.mapToLocation(request.location());
            event.setLocation(location);
        }

        if (request.paid() != null) {
            event.setPaid(request.paid());
        }

        if (request.participantLimit() != null) {
            event.setParticipantLimit(request.participantLimit());
        }

        if (request.requestModeration() != null) {
            event.setRequestModeration(request.requestModeration());
        }

        if (request.title() != null && !request.title().isBlank()) {
            event.setTitle(request.title());
        }

    }

}
