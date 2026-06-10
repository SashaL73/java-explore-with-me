package ru.practicum.ewm.user.dto;

import lombok.Builder;

@Builder
public record UserShortDto(Long id, String name) {
}
