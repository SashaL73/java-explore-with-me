package ru.practicum.ewm.user.mapper;

import ru.practicum.ewm.user.dto.SubscriptionDto;

import java.util.List;

public class SubscriptionMapper {

    public static SubscriptionDto mapToSubscriptionDto(List<Long> usersTargetIds, Long subscriberId) {
        return SubscriptionDto.builder()
                .subscriberId(subscriberId)
                .targetUserIds(usersTargetIds)
                .build();
    }
}
