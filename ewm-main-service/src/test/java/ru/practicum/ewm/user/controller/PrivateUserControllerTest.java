package ru.practicum.ewm.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.user.dto.SubscriptionDto;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.UserStatus;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.ewm.user.service.UserSubscriptionService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(PrivateUserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PrivateUserControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private EventService eventService;

    @MockBean
    private UserSubscriptionService userSubscriptionService;

    private final SubscriptionDto subscriptionDto = SubscriptionDto.builder()
            .subscriberId(1L)
            .targetUserIds(List.of(2L, 3L))
            .build();

    private final EventShortDto eventShortDto = EventShortDto.builder()
            .annotation("TestAnnotation")
            .id(1L)
            .title("Test")
            .category(CategoryDto.builder()
                    .name("TestCategory")
                    .id(1L)
                    .build())
            .confirmedRequests(1L)
            .eventDate(LocalDateTime.of(2026, 6, 6, 6, 1))
            .initiator(UserShortDto.builder()
                    .id(2L)
                    .name("TestName")
                    .build())
            .views(0L)
            .paid(true)
            .build();

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("Test")
            .email("test@test.com")
            .status(UserStatus.PRIVATE)
            .build();

    @Test
    void subscribeUserTest() throws Exception {
        mockMvc.perform(post("/subscriptions/{userId}/subscribe/{userTargetId}", 1L, 2L))
                .andExpect(status().isCreated());
    }

    @Test
    void subscribeUserIncorrectPathShouldReturnBadRequestTest() throws Exception {
        mockMvc.perform(post("/subscriptions/{userId}/subscribe/{userTargetId}", 0L, 2L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/subscriptions/{userId}/subscribe/{userTargetId}", 1L, 0L))
                .andExpect(status().isBadRequest());

    }

    @Test
    void getSubscriptionsTest() throws Exception {
        when(userSubscriptionService.getSubscriptions(anyLong()))
                .thenReturn(subscriptionDto);

        mockMvc.perform(get("/subscriptions/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriberId").value(1))
                .andExpect(jsonPath("$.targetUserIds", hasSize(2)))
                .andExpect(jsonPath("$.targetUserIds.[0]").value(2))
                .andExpect(jsonPath("$.targetUserIds.[1]").value(3));
    }

    @Test
    void getSubscriptionsIncorrectPathShouldReturnBadRequestTest() throws Exception {
        mockMvc.perform(get("/subscriptions/{userId}", 0L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSubscriptionEventsTest() throws Exception {
        List<EventShortDto> shortDtoList = List.of(eventShortDto);

        when(eventService.getSubscriptionEvents(anyLong()))
                .thenReturn(shortDtoList);

        mockMvc.perform(get("/subscriptions/{userId}/events", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].annotation").value("TestAnnotation"))
                .andExpect(jsonPath("$.[0].category.id").value(1))
                .andExpect(jsonPath("$.[0].initiator.id").value(2));
    }

    @Test
    void getSubscriptionEventsIncorrectShouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/subscriptions/{userId}/events", 0L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unsubscribeTest() throws Exception {
        mockMvc.perform(delete("/subscriptions/{userId}/subscribe/{userTargetId}", 1L, 2L))
                .andExpect(status().isNoContent());

    }

    @Test
    void unsubscribeIncorrectPathShouldReturnBadRequestTest() throws Exception {
        mockMvc.perform(delete("/subscriptions/{userId}/subscribe/{userTargetId}", 0L, 2L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/subscriptions/{userId}/subscribe/{userTargetId}", 1L, 0L))
                .andExpect(status().isBadRequest());

    }

    @Test
    void setStatusTest() throws Exception {
        when(userService.setStatus(anyLong(), any(UserStatus.class)))
                .thenReturn(userDto);

        mockMvc.perform(patch("/subscriptions/{userId}/status", 1L)
                        .param("status", String.valueOf(UserStatus.PRIVATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.status").value("PRIVATE"));
    }

    @Test
    void setStatusIncorrectPathShouldReturnBadRequestTest() throws Exception {
        mockMvc.perform(patch("/subscriptions/{userId}/status", 0L)
                        .param("status", String.valueOf(UserStatus.PRIVATE)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setStatusIncorrectParamShouldReturnBadRequestTest() throws Exception {
        mockMvc.perform(patch("/subscriptions/{userId}/status", 1L)
                        .param("status", "test"))
                .andExpect(status().isBadRequest());
    }


}
