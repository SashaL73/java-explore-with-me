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
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.UserStateAction;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.service.ParticipationService;
import ru.practicum.ewm.user.dto.UserShortDto;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(PrivateEventController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PrivateEventControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private final EventService eventService;

    @MockBean
    private final ParticipationService participationService;

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
                    .id(1L)
                    .name("TestName")
                    .build())
            .views(0L)
            .paid(true)
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

    private final ParticipationRequestDto participationRequestDto = ParticipationRequestDto.builder()
            .created(LocalDateTime.of(2026, 6, 6, 1, 1, 0))
            .event(1L)
            .id(1L)
            .requester(1L)
            .status(ParticipationStatus.CONFIRMED)
            .build();

    private final EventRequestStatusUpdateResult eventRequestStatusUpdateResult = EventRequestStatusUpdateResult.builder()
            .confirmedRequests(List.of(participationRequestDto))
            .rejectedRequests(List.of())
            .build();

    private LocationDto createLocationDto(Float lat, Float lon) {
        return LocationDto.builder()
                .lat(lat)
                .lon(lon)
                .build();
    }

    private NewEventDto createNewEventDto(String annotation,
                                          Long category,
                                          String description,
                                          LocalDateTime eventDate,
                                          LocationDto location,
                                          Long limit,
                                          String title) {
        return NewEventDto.builder()
                .annotation(annotation)
                .category(category)
                .description(description)
                .eventDate(eventDate)
                .location(location)
                .paid(true)
                .participantLimit(limit)
                .requestModeration(true)
                .title(title)
                .build();
    }

    private UpdateEventUserRequest createUpdateEventUserRequest(String annotation,
                                                                Long category,
                                                                String description,
                                                                LocationDto location,
                                                                Long limit,
                                                                String title) {
        return UpdateEventUserRequest.builder()
                .annotation(annotation)
                .category(category)
                .description(description)
                .location(location)
                .paid(true)
                .participantLimit(limit)
                .requestModeration(true)
                .stateAction(UserStateAction.SEND_TO_REVIEW)
                .title(title)
                .build();
    }


    private EventRequestStatusUpdateRequest createStatusUpdateRequest(List<Long> requestsIds, ParticipationStatus status) {
        return EventRequestStatusUpdateRequest.builder()
                .requestIds(requestsIds)
                .status(status)
                .build();
    }


    @Test
    void getEventsTest() throws Exception {
        when(eventService.getEventsByIdInitiator(
                Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.anyLong()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/{userId}/events", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test"))
                .andExpect(jsonPath("$[0].annotation").value("TestAnnotation"))
                .andExpect(jsonPath("$[0].category.id").value(1))
                .andExpect(jsonPath("$[0].category.name").value("TestCategory"))
                .andExpect(jsonPath("$[0].initiator.id").value(1))
                .andExpect(jsonPath("$[0].initiator.name").value("TestName"))
                .andExpect(jsonPath("$[0].confirmedRequests").value(1))
                .andExpect(jsonPath("$[0].paid").value(true))
                .andExpect(jsonPath("$[0].views").value(0));

        Mockito.verify(eventService).getEventsByIdInitiator(1L, 0L, 10L);
    }

    @Test
    void getEventsIncorrectPathTest() throws Exception {
        when(eventService.getEventsByIdInitiator(
                Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.anyLong()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/{userId}/events", 0L))
                .andExpect(status().isBadRequest());

    }

    @Test
    void createEventTest() throws Exception {
        LocationDto locationDto = LocationDto.builder()
                .lat(10.1F)
                .lon(10.1F)
                .build();
        NewEventDto newEventDto = createNewEventDto("t".repeat(20),
                1L, "t".repeat(20), LocalDateTime.now(), locationDto, 0L, "test");
        when(eventService.createEvent(Mockito.anyLong(), Mockito.any(NewEventDto.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.location.lat").value(10.1))
                .andExpect(jsonPath("$.location.lon").value(10.1))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.initiator.id").value(1));
    }

    @Test
    void createEventIncorrectBodyAnnotationSizeTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        NewEventDto newEventDto1 = createNewEventDto("t",
                1L, "t".repeat(20), LocalDateTime.now(), locationDto, 0L, "test");

        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto1))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEventIncorrectBodyAnnotationBlancTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        NewEventDto newEventDto = createNewEventDto(" ",
                1L, "t".repeat(20), LocalDateTime.now(), locationDto, 0L, "test");


        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEventIncorrectBodyCategoryNullTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        NewEventDto newEventDto = createNewEventDto("t".repeat(20),
                null, "t".repeat(20), LocalDateTime.now(), locationDto, 0L, "test");


        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEventIncorrectBodyCategoryNegativeTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        NewEventDto newEventDto = createNewEventDto("t".repeat(20),
                0L, "t".repeat(20), LocalDateTime.now(), locationDto, 0L, "test");


        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEventIncorrectBodyDescriptionSizeTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        NewEventDto newEventDto = createNewEventDto("t".repeat(20),
                1L, "t", LocalDateTime.now(), locationDto, 0L, "test");


        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEventIncorrectBodyDescriptionBlancTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        NewEventDto newEventDto = createNewEventDto("t".repeat(20),
                1L, " ", LocalDateTime.now(), locationDto, 0L, "test");


        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEventIncorrectBodyLocationLatTest() throws Exception {
        LocationDto locationDto = createLocationDto(null, 10.1F);
        NewEventDto newEventDto = createNewEventDto("t".repeat(20),
                1L, "t".repeat(20), LocalDateTime.now(), locationDto, 0L, "test");


        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEventIncorrectBodyLocationLonTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, null);
        NewEventDto newEventDto = createNewEventDto("t".repeat(20),
                1L, "t".repeat(20), LocalDateTime.now(), locationDto, 0L, "test");


        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEventIncorrectBodyParticipationLimitNegativeTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        NewEventDto newEventDto = createNewEventDto("t".repeat(20),
                1L, "t".repeat(20), LocalDateTime.now(), locationDto, -1L, "test");


        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEventIncorrectBodyTitleSizeTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        NewEventDto newEventDto = createNewEventDto("t".repeat(20),
                1L, "t".repeat(20), LocalDateTime.now(), locationDto, 1L, "te");


        mockMvc.perform(post("/users/{userId}/events", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEventByOwnerTest() throws Exception {
        when(eventService.getEventByOwner(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/users/{userId}/events/{eventId}", 1, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.location.lat").value(10.1))
                .andExpect(jsonPath("$.location.lon").value(10.1))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.initiator.id").value(1));
    }

    @Test
    void getEventByOwnerIncorrectPathTest() throws Exception {
        mockMvc.perform(get("/users/{userId}/events/{eventId}", 0, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/users/{userId}/events/{eventId}", 1, 0))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEventByOwnerTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        when(eventService.updateEventByOwner(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        UpdateEventUserRequest userRequest = createUpdateEventUserRequest("t".repeat(20), 1L,
                "d".repeat(20), locationDto, 0L, "t".repeat(20));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.location.lat").value(10.1))
                .andExpect(jsonPath("$.location.lon").value(10.1))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.initiator.id").value(1));
    }

    @Test
    void updateEventByOwnerIncorrectBodyAnnotationTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        when(eventService.updateEventByOwner(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        UpdateEventUserRequest userRequest = createUpdateEventUserRequest("t", 1L,
                "d".repeat(20), locationDto, 0L, "t".repeat(20));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEventByOwnerIncorrectBodyCategoryTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        when(eventService.updateEventByOwner(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        UpdateEventUserRequest userRequest = createUpdateEventUserRequest("t".repeat(20), 0L,
                "d".repeat(20), locationDto, 0L, "t".repeat(20));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEventByOwnerIncorrectBodyDescriptionTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        when(eventService.updateEventByOwner(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        UpdateEventUserRequest userRequest = createUpdateEventUserRequest("t".repeat(20), 1L,
                "d", locationDto, 0L, "t".repeat(20));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEventByOwnerIncorrectBodyParticipationLimitTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        when(eventService.updateEventByOwner(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        UpdateEventUserRequest userRequest = createUpdateEventUserRequest("t".repeat(20), 1L,
                "d".repeat(20), locationDto, -1L, "t".repeat(20));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEventByOwnerIncorrectBodyTitleTest() throws Exception {
        LocationDto locationDto = createLocationDto(10.1F, 10.1F);
        when(eventService.updateEventByOwner(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        UpdateEventUserRequest userRequest = createUpdateEventUserRequest("t".repeat(20), 1L,
                "d".repeat(20), locationDto, 1L, "t");

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestsByOwnerEventTest() throws Exception {
        when(participationService.getRequestsByOwnerEvent(
                Mockito.anyLong(),
                Mockito.anyLong()))
                .thenReturn(List.of(participationRequestDto));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].created")
                        .value("2026-06-06T01:01:00"));
    }

    @Test
    void getRequestsByOwnerEventIncorrectPathTest() throws Exception {
        when(participationService.getRequestsByOwnerEvent(
                Mockito.anyLong(),
                Mockito.anyLong()))
                .thenReturn(List.of(participationRequestDto));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", 0, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", 1, 0))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusRequestsByOwnerEventTest() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = createStatusUpdateRequest(List.of(1L),
                ParticipationStatus.CONFIRMED);

        when(participationService.updateStatusRequestsByOwnerEvent(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(eventRequestStatusUpdateResult);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", 1, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests", hasSize(1)))
                .andExpect(jsonPath("$.rejectedRequests", hasSize(0)));
    }

    @Test
    void updateStatusRequestsByOwnerEventBodyNegativeIdsTest() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = createStatusUpdateRequest(List.of(0L),
                ParticipationStatus.CONFIRMED);

        when(participationService.updateStatusRequestsByOwnerEvent(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(eventRequestStatusUpdateResult);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", 1, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusRequestsByOwnerEventEmptyNullIdsTest() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = createStatusUpdateRequest(List.of(),
                ParticipationStatus.CONFIRMED);

        when(participationService.updateStatusRequestsByOwnerEvent(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(eventRequestStatusUpdateResult);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", 1, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusRequestsByOwnerEventStatusNullTest() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = createStatusUpdateRequest(List.of(1L),
                null);

        when(participationService.updateStatusRequestsByOwnerEvent(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(eventRequestStatusUpdateResult);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", 1, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


}
