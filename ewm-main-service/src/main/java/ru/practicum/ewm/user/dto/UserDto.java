package ru.practicum.ewm.user.dto;

import lombok.Builder;

@Builder
public record UserDto(Long id, String email, String name) {
}
