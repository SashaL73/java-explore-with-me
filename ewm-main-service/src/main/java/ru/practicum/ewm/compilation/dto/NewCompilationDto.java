package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record NewCompilationDto(
        List<@Positive(message = "Id события должен быть положительным") Long> events,
        Boolean pinned,
        @NotBlank(message = "Заголовок подборки должен быть указана")
        @Size(min = 1, max = 50, message = "Заголовок подборки должен быть от 1, до 50 символов")
        String title
) {
}
