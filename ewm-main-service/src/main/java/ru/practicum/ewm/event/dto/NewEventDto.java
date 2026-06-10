package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NewEventDto(
        @NotBlank(message = "Описание должно быть указано")
        @Size(min = 20, max = 2000, message = "Краткое описание события должно быть от 20 до 2000 сиволов")
        String annotation,
        @NotNull(message = "Id категории должен быть указан")
        @Positive(message = "Id категории должен быть положительным")
        Long category,
        @NotBlank(message = "Полное описание события должно быть указано")
        @Size(min = 20, max = 7000, message = "Полное описание события должно быть от 20 до 7000 символов")
        String description,
        @NotNull(message = "Дата и время на которые намечено событие должно быть указано")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,
        @NotNull(message = "Широта и долгота места проведения события должны быть указаны")
        @Valid
        LocationDto location,
        Boolean paid,
        @PositiveOrZero
        Long participantLimit,
        Boolean requestModeration,
        @NotBlank(message = "Заголовок события должен быть указан")
        @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов")
        String title
) {
}
