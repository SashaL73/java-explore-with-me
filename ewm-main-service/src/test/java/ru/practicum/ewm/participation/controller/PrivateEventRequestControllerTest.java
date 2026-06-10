package ru.practicum.ewm.participation.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.service.ParticipationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrivateEventRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PrivateEventRequestControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private ParticipationService participationService;

    private final ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
            .created(LocalDateTime.now())
            .event(1L)
            .id(1L)
            .requester(1L)
            .status(ParticipationStatus.PENDING)
            .build();

    @Test
    void getParticipationTest() throws Exception {
        List<ParticipationRequestDto> requestDtoList = List.of(requestDto);

        when(participationService.getParticipation(Mockito.anyLong()))
                .thenReturn(requestDtoList);

        mockMvc.perform(get("/users/{userId}/requests", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].event").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].requester").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

    }

    @Test
    void getParticipationWithIncorrectPathShouldReturnBadRequestTest() throws Exception {
        mockMvc.perform(get("/users/{userId}/requests", 0L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createParticipationTest() throws Exception {
        when(participationService.createParticipation(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.event").value(1))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.requester").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

    }

    @Test
    void createParticipationWithIncorrectPathAndParamShouldReturnBadRequestTest() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", 0L)
                        .param("eventId", "1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/users/{userId}/requests", 1L))
                .andExpect(status().isBadRequest());

    }

    @Test
    void cancelParticipationTest() throws Exception {
        when(participationService.cancelParticipation(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event").value(1))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.requester").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

    }

    @Test
    void cancelParticipationWithIncorrectPathShouldReturnBadRequestTest() throws Exception {
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 0L, 1L))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 0L))
                .andExpect(status().isBadRequest());
    }


}
