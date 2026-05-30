package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.model.SortEvent;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.user.dto.UserShortDto;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebMvcTest(PublicEventController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PublicEventControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    private final EventShortDto eventShortDto = EventShortDto.builder()
            .annotation("Test")
            .category(CategoryDto.builder()
                    .id(1L)
                    .name("test")
                    .build())
            .confirmedRequests(1L)
            .eventDate(LocalDateTime.of(2026, 6, 6, 1, 1))
            .id(1L)
            .initiator(UserShortDto.builder()
                    .id(1L)
                    .name("Test")
                    .build())
            .paid(true)
            .title("TestTitle")
            .views(0L)
            .build();

    private final EventFullDto eventFullDto = EventFullDto.builder()
            .id(1L)
            .category(CategoryDto.builder()
                    .id(1L)
                    .name("test")
                    .build())
            .confirmedRequests(1L)
            .eventDate(LocalDateTime.of(2026, 6, 6, 1, 1))
            .initiator(UserShortDto.builder()
                    .id(1L)
                    .name("Test")
                    .build())
            .location(LocationDto.builder()
                    .lat(10.1F)
                    .lon(10.1F)
                    .build())
            .paid(true)
            .title("TestTitle")
            .views(0L)
            .build();


    @Test
    void getEventsTest() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime rangeStart = LocalDateTime.of(2026, 6, 6, 10, 0, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2026, 6, 7, 10, 0, 0);

        when(eventService.getEvents(
                Mockito.anyString(),
                Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.anyBoolean(),
                Mockito.any(SortEvent.class),
                Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.any(HttpServletRequest.class)))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/events")
                        .param("text", "Test")
                        .param("categories", "1")
                        .param("paid", "true")
                        .param("rangeStart", rangeStart.format(formatter))
                        .param("rangeEnd", rangeEnd.format(formatter))
                        .param("onlyAvailable", "true")
                        .param("sort", SortEvent.EVENT_DATE.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value(eventShortDto.title()));
    }

    @Test
    void getEventByIdTest() throws Exception {
        when(eventService.getEventById(Mockito.anyLong(), Mockito.any(HttpServletRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/events/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value(eventFullDto.title()))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("test"))
                .andExpect(jsonPath("$.initiator.id").value(1))
                .andExpect(jsonPath("$.initiator.name").value("Test"));
    }

    @Test
    void getEventByIdIncorrectPathTest() throws Exception {
        mockMvc.perform(get("/events/{id}", 0L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


}
