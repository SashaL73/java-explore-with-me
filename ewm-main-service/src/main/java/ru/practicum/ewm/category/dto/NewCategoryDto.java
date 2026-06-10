package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record NewCategoryDto(
        @NotBlank(message = "Название категории должно быть указано")
        @Size(min = 1, max = 50, message = "Длинна категории должна быть от 1 до 50 сиволов")
        String name
) {
}
