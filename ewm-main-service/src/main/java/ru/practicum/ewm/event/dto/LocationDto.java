package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LocationDto(
        @NotNull(message = "Широта должна быть указана")
        Float lat,
        @NotNull(message = "Долгота должна быть указана")
        Float lon
) {
}
