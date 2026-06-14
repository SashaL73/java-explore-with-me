package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.SubscriptionDto;

public interface UserSubscriptionService {

    void subscribe(Long userId, Long targetUserId);

    void unsubscribe(Long userId, Long targetUserId);

    SubscriptionDto getSubscriptions(Long userId);
}
