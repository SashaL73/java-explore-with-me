package ru.practicum.ewm.participation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import ru.practicum.ewm.participation.model.ParticipationStatus;

import java.util.List;

@Builder
public record EventRequestStatusUpdateRequest(
        @NotEmpty
        List<@Positive(message = "id должны быть положительными") Long> requestIds,
        @NotNull
        ParticipationStatus status
) {
}
