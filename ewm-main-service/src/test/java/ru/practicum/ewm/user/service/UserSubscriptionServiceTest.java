package ru.practicum.ewm.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.user.dto.SubscriptionDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.UserStatus;
import ru.practicum.ewm.user.model.UserSubscription;
import ru.practicum.ewm.user.model.UserSubscriptionId;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.repository.UserSubscriptionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserSubscriptionServiceTest {

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSubscriptionServiceImpl userSubscriptionService;

    private User createUser(Long id, UserStatus status) {
        return User.builder()
                .name("Test")
                .email("test@test.com")
                .status(status)
                .id(id)
                .build();
    }

    private UserSubscription createUserSubscription(Long userId, Long targetUserId, UserStatus status) {
        return UserSubscription.builder()
                .subscriber(User.builder()
                        .id(userId)
                        .name("Test" + userId)
                        .email("test" + userId + "@test.com")
                        .status(status)
                        .build())
                .targetUser(User.builder()
                        .id(targetUserId)
                        .name("Test" + targetUserId)
                        .email("test" + targetUserId + "@test.com")
                        .status(status)
                        .build())
                .build();
    }


    @Test
    void subscribeTest() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.ofNullable(createUser(1L, UserStatus.PUBLIC)));

        when(userRepository.findById(2L))
                .thenReturn(Optional.ofNullable(createUser(2L, UserStatus.PUBLIC)));

        userSubscriptionService.subscribe(1L, 2L);

        ArgumentCaptor<UserSubscription> captor = ArgumentCaptor.forClass(UserSubscription.class);
        verify(userSubscriptionRepository).save(captor.capture());

        UserSubscription userSubscription = captor.getValue();

        assertEquals(1L, userSubscription.getSubscriber().getId());
        assertEquals(2L, userSubscription.getTargetUser().getId());

        assertEquals(1L, userSubscription.getId().getUserSubscriberId());
        assertEquals(2L, userSubscription.getId().getUserTargetId());

    }

    @Test
    void subscribeUserIdSameTargetIdShouldReturnConflictExceptionTest() {
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userSubscriptionService.subscribe(1L, 1L));

        assertEquals("Пользователь не может подписаться на себя", exception.getMessage());
    }

    @Test
    void subscribeUserExistSubscriptionOnTargetIdShouldReturnConflictExceptionTest() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.ofNullable(createUser(1L, UserStatus.PUBLIC)));

        when(userRepository.findById(2L))
                .thenReturn(Optional.ofNullable(createUser(2L, UserStatus.PUBLIC)));

        when(userSubscriptionRepository.existsBySubscriberIdAndTargetUserId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userSubscriptionService.subscribe(1L, 2L));

        assertEquals("Вы уже подписаны на этого пользователя", exception.getMessage());

    }

    @Test
    void subscribeUserTargetIdPrivateShouldReturnConflictExceptionTest() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.ofNullable(createUser(1L, UserStatus.PUBLIC)));

        when(userRepository.findById(2L))
                .thenReturn(Optional.ofNullable(createUser(2L, UserStatus.PRIVATE)));

        when(userSubscriptionRepository.existsBySubscriberIdAndTargetUserId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(false);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userSubscriptionService.subscribe(1L, 2L));

        assertEquals("Нельзя подписаться на приватного пользователя", exception.getMessage());
    }

    @Test
    void unsubscribeTest() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.ofNullable(createUser(1L, UserStatus.PUBLIC)));

        when(userRepository.findById(2L))
                .thenReturn(Optional.ofNullable(createUser(2L, UserStatus.PUBLIC)));

        when(userSubscriptionRepository.existsBySubscriberIdAndTargetUserId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(true);

        userSubscriptionService.unsubscribe(1L, 2L);

        ArgumentCaptor<UserSubscriptionId> captor = ArgumentCaptor.forClass(UserSubscriptionId.class);
        verify(userSubscriptionRepository).deleteById(captor.capture());
        UserSubscriptionId userSubscriptionId = captor.getValue();

        assertEquals(1L, userSubscriptionId.getUserSubscriberId());
        assertEquals(2L, userSubscriptionId.getUserTargetId());
    }

    @Test
    void unsubscribeNotExistSubscribeTest() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.ofNullable(createUser(1L, UserStatus.PUBLIC)));

        when(userRepository.findById(2L))
                .thenReturn(Optional.ofNullable(createUser(2L, UserStatus.PUBLIC)));

        when(userSubscriptionRepository.existsBySubscriberIdAndTargetUserId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userSubscriptionService.unsubscribe(1L, 2L));

        assertEquals("Вы не подписаны на этого пользователя", exception.getMessage());
    }

    @Test
    void getSubscriptionsTest() {
        List<UserSubscription> userSubscriptions = List.of(
                createUserSubscription(1L, 2L, UserStatus.PUBLIC),
                createUserSubscription(1L, 3L, UserStatus.PUBLIC)
        );

        when(userSubscriptionRepository.findAllBySubscriberId(Mockito.anyLong()))
                .thenReturn(userSubscriptions);

        SubscriptionDto subscriptionDto = userSubscriptionService.getSubscriptions(1L);

        assertEquals(1L, subscriptionDto.subscriberId());
        assertEquals(2L, subscriptionDto.targetUserIds().size());
        assertEquals(2L, subscriptionDto.targetUserIds().getFirst());
        assertEquals(3L, subscriptionDto.targetUserIds().getLast());
    }


}
