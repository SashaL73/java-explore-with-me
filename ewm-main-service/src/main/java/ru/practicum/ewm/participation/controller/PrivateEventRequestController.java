package ru.practicum.ewm.participation.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.service.ParticipationService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@Validated
@RequiredArgsConstructor
public class PrivateEventRequestController {

    private final ParticipationService participationService;

    @GetMapping
    public List<ParticipationRequestDto> getParticipation(
            @PathVariable
            @Positive(message = "id пользователя должен быть положительным")
            Long userId) {
        return participationService.getParticipation(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createParticipation(
            @PathVariable
            @Positive(message = "id пользователя должен быть положительным")
            Long userId,
            @RequestParam(value = "eventId")
            @Positive(message = "eventId должен быть положительным")
            Long eventId) {
        return participationService.createParticipation(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelParticipation(
            @PathVariable
            @Positive(message = "id пользователя должен быть положительным")
            Long userId,
            @PathVariable
            @Positive(message = "id заявки должен быть положительным")
            Long requestId) {
        return participationService.cancelParticipation(userId, requestId);
    }

}
