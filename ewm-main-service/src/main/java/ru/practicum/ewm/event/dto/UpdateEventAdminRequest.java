package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.practicum.ewm.event.model.AdminStateAction;

import java.time.LocalDateTime;

@Builder
public record UpdateEventAdminRequest(
        @Size(min = 20, max = 2000, message = "Краткое описание события должно быть от 20 до 2000 сиволов")
        String annotation,
        @Positive(message = "Id категории должен быть положительным")
        Long category,
        @Size(min = 20, max = 7000, message = "Полное описание события должно быть от 20 до 7000 символов")
        String description,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,
        @Valid
        LocationDto location,
        Boolean paid,
        Long participantLimit,
        Boolean requestModeration,
        AdminStateAction stateAction,
        @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов")
        String title
) {
}
