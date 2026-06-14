package ru.practicum.ewm.user.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.user.dto.SubscriptionDto;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.UserStatus;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.user.service.UserSubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/subscriptions/{userId}")
@RequiredArgsConstructor
@Validated
public class PrivateUserController {

    private final UserSubscriptionService userSubscriptionService;
    private final EventService eventService;
    private final UserService userService;


    @PostMapping("/subscribe/{userTargetId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(
            @PathVariable @Positive(message = "id должен быть положительным") Long userId,
            @PathVariable @Positive(message = "id должен быть положительным") Long userTargetId) {

        userSubscriptionService.subscribe(userId, userTargetId);
    }

    @GetMapping
    public SubscriptionDto getSubscriptions(
            @PathVariable @Positive(message = "id должен быть положительным") Long userId) {

        return userSubscriptionService.getSubscriptions(userId);
    }

    @GetMapping("/events")
    public List<EventShortDto> getSubscriptionEvents(
            @PathVariable @Positive(message = "id должен быть положительным") Long userId) {
        return eventService.getSubscriptionEvents(userId);
    }

    @DeleteMapping("/subscribe/{userTargetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable @Positive(message = "id должен быть положительным") Long userId,
                            @PathVariable @Positive(message = "id должен быть положительным") Long userTargetId) {
        userSubscriptionService.unsubscribe(userId, userTargetId);

    }

    @PatchMapping("/status")
    public UserDto setStatus(@PathVariable @Positive(message = "id должен быть положительным") Long userId,
                             @RequestParam(name = "status") UserStatus status) {
        return userService.setStatus(userId, status);
    }


}
