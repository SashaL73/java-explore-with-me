package ru.practicum.ewm.user.dto;

import lombok.Builder;
import ru.practicum.ewm.user.model.UserStatus;

@Builder
public record UserDto(Long id, String email, String name, UserStatus status) {
}
