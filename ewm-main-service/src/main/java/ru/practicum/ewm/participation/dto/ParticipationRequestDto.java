package ru.practicum.ewm.participation.dto;

import lombok.Builder;
import ru.practicum.ewm.participation.model.ParticipationStatus;

import java.time.LocalDateTime;

@Builder
public record ParticipationRequestDto(
        LocalDateTime created,
        Long event,
        Long id,
        Long requester,
        ParticipationStatus status
) {
}
