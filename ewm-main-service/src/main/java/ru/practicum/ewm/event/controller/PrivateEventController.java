package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.service.ParticipationService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {

    private final EventService eventService;
    private final ParticipationService participationService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @PathVariable @Positive(message = "id должен быть положительным") Long userId,
            @RequestParam(value = "from", defaultValue = "0") Long from,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        return eventService.getEventsByIdInitiator(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(
            @PathVariable @Positive(message = "id должен быть положительным") Long userId,
            @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByOwner(
            @PathVariable @Positive(message = "id должен быть положительным") Long userId,
            @PathVariable @Positive(message = "id должен быть положительным") Long eventId) {
        return eventService.getEventByOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByOwner(
            @PathVariable @Positive(message = "id должен быть положительным") Long userId,
            @PathVariable @Positive(message = "id должен быть положительным") Long eventId,
            @RequestBody @Valid UpdateEventUserRequest userRequest) {
        return eventService.updateEventByOwner(userId, eventId, userRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByOwnerEvent(
            @PathVariable @Positive(message = "id должен быть положительным") Long userId,
            @PathVariable @Positive(message = "id должен быть положительным") Long eventId) {
        return participationService.getRequestsByOwnerEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusRequestsByOwnerEvent(
            @PathVariable @Positive(message = "id должен быть положительным") Long userId,
            @PathVariable @Positive(message = "id должен быть положительным") Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        return participationService.updateStatusRequestsByOwnerEvent(userId, eventId, updateRequest);
    }


}
