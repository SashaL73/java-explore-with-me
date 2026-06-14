package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.user.dto.SubscriptionDto;
import ru.practicum.ewm.user.mapper.SubscriptionMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.UserStatus;
import ru.practicum.ewm.user.model.UserSubscription;
import ru.practicum.ewm.user.model.UserSubscriptionId;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.repository.UserSubscriptionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public void subscribe(Long userId, Long targetUserId) {
        log.info("Добавление подписки пользователем userId={} на пользователя targetUserId={}", userId, targetUserId);
        findUserOrThrow(targetUserId);
        if (userId.equals(targetUserId)) {
            throw new ConflictException("Пользователь не может подписаться на себя");
        }

        User subscriber = findUserOrThrow(userId);
        User targetUser = findUserOrThrow(targetUserId);

        if (userSubscriptionRepository.existsBySubscriberIdAndTargetUserId(userId, targetUserId)) {
            throw new ConflictException("Вы уже подписаны на этого пользователя");
        }

        if (targetUser.getStatus() != UserStatus.PUBLIC) {
            throw new ConflictException("Нельзя подписаться на приватного пользователя");
        }

        UserSubscription userSubscription = UserSubscription.builder()
                .id(UserSubscriptionId.builder()
                        .userSubscriberId(userId)
                        .userTargetId(targetUserId)
                        .build())
                .subscriber(subscriber)
                .targetUser(targetUser)
                .build();

        userSubscriptionRepository.save(userSubscription);

    }

    @Override
    @Transactional
    public void unsubscribe(Long userId, Long targetUserId) {
        log.info("Удаление подписки пользователя userId={} на пользователя targetUserId={}", userId, targetUserId);
        User subscriber = findUserOrThrow(userId);
        User targetUser = findUserOrThrow(targetUserId);

        if (!userSubscriptionRepository.existsBySubscriberIdAndTargetUserId(userId, targetUserId)) {
            throw new NotFoundException("Вы не подписаны на этого пользователя");
        }

        UserSubscriptionId userSubscriptionId = UserSubscriptionId.builder()
                .userSubscriberId(subscriber.getId())
                .userTargetId(targetUser.getId())
                .build();

        userSubscriptionRepository.deleteById(userSubscriptionId);

    }

    @Override
    public SubscriptionDto getSubscriptions(Long userId) {
        log.info("Получение подписок пользователя userId={}", userId);
        List<UserSubscription> userSubscriptions = userSubscriptionRepository.findAllBySubscriberId(userId);

        List<Long> targetUserId = userSubscriptions.stream()
                .map(userSubscription -> userSubscription.getTargetUser().getId())
                .toList();

        return SubscriptionMapper.mapToSubscriptionDto(targetUserId, userId);
    }


    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с id = " + id + " нет"));

    }
}
