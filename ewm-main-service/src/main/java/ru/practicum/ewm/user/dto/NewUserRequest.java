package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record NewUserRequest(
        @NotBlank(message = "email должен быть указан")
        @Email(message = "некорректный email")
        @Size(min = 6, max = 254)
        String email,
        @NotBlank(message = "Имя должно быть указано")
        @Size(min = 2, max = 250)
        String name) {
}
