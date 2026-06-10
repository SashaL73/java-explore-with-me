package ru.practicum.ewm.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.user.dto.UserShortDto;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@WebMvcTest(AdminEventController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AdminEventControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private final EventService eventService;

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

    private UpdateEventAdminRequest createUpdateEventAdminRequest(String annotation,
                                                                  Long category,
                                                                  String description,
                                                                  LocationDto location,
                                                                  String title) {
        return UpdateEventAdminRequest.builder()
                .annotation(annotation)
                .category(category)
                .description(description)
                .location(location)
                .title(title)
                .build();

    }

    private LocationDto createLocationDto(Float lat, Float lon) {
        return LocationDto.builder()
                .lat(lat)
                .lon(lon)
                .build();
    }

    @Test
    void getEventsTest() throws Exception {
        when(eventService.getEventsAdmin(Mockito.anyList(), Mockito.anyList(), Mockito.anyList(),
                Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(List.of(eventFullDto));

        mockMvc.perform(get("/admin/events")
                        .param("users", "1")
                        .param("states", "PUBLISHED")
                        .param("categories", "1")
                        .param("rangeStart", "2026-01-01 10:00:00")
                        .param("rangeEnd", "2026-01-02 10:00:00")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateEventTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);

        UpdateEventAdminRequest request = createUpdateEventAdminRequest(
                "t".repeat(20), 1L, "d".repeat(20), locationDto, "t".repeat(3)
        );

        when(eventService.updateEventAdmin(Mockito.anyLong(), Mockito.any(UpdateEventAdminRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/admin/events/{eventId}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.initiator.id").value(1))
                .andExpect(jsonPath("$.title").value("TestTitle"));

    }

    @Test
    void updateEventIncorrectPathTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);

        UpdateEventAdminRequest request = createUpdateEventAdminRequest(
                "t".repeat(20), 1L, "d".repeat(20), locationDto, "t".repeat(3)
        );

        mockMvc.perform(patch("/admin/events/{eventId}", 0)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void updateEventIncorrectBodyAnnotationTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);

        UpdateEventAdminRequest request = createUpdateEventAdminRequest(
                "t", 1L, "d".repeat(20), locationDto, "t".repeat(3)
        );

        mockMvc.perform(patch("/admin/events/{eventId}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void updateEventIncorrectBodyCategoryTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);

        UpdateEventAdminRequest request = createUpdateEventAdminRequest(
                "t".repeat(20), 0L, "d".repeat(20), locationDto, "t".repeat(3)
        );

        mockMvc.perform(patch("/admin/events/{eventId}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void updateEventIncorrectBodyDescriptionTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);

        UpdateEventAdminRequest request = createUpdateEventAdminRequest(
                "t".repeat(20), 1L, "d", locationDto, "t".repeat(3)
        );

        mockMvc.perform(patch("/admin/events/{eventId}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void updateEventIncorrectBodyTitleTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);

        UpdateEventAdminRequest request = createUpdateEventAdminRequest(
                "t".repeat(20), 1L, "d".repeat(20), locationDto, "t"
        );

        mockMvc.perform(patch("/admin/events/{eventId}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void updateEventIncorrectBodyLocationLatTest() throws Exception {
        LocationDto locationDto = createLocationDto(null, 10.1F);

        UpdateEventAdminRequest request = createUpdateEventAdminRequest(
                "t".repeat(20), 1L, "d".repeat(20), locationDto, "t".repeat(20)
        );

        mockMvc.perform(patch("/admin/events/{eventId}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void updateEventIncorrectBodyLocationLonTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, null);

        UpdateEventAdminRequest request = createUpdateEventAdminRequest(
                "t".repeat(20), 1L, "d".repeat(20), locationDto, "t".repeat(20)
        );

        mockMvc.perform(patch("/admin/events/{eventId}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }


}


