package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.exception.BadRequestException;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.service.view.EventViews;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.participation.repository.projection.RequestsCountProjection;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.svc.EndpointHit;
import ru.practicum.svc.StatsClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StatsClient statsClient;

    @Mock
    private EventViews eventViews;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private EventServiceImpl eventService;

    private User createUser() {
        return User.builder()
                .id(1L)
                .name("Test")
                .email("test@test.com")
                .build();
    }

    private Category createCategory() {
        return Category.builder()
                .id(1L)
                .name("Test")
                .build();
    }

    private Location createLocation() {
        return Location.builder()
                .lat(10.1F)
                .lon(10.1F)
                .build();
    }

    private LocationDto createLocationDto() {
        return LocationDto.builder()
                .lat(10.1F)
                .lon(10.1F)
                .build();
    }

    private Event createEvent(EventState state) {
        return createEvent(1L, state, "Test");
    }

    private Event createEvent(Long id, EventState state, String title) {
        return Event.builder()
                .id(id)
                .annotation("Test")
                .description("Test")
                .eventDate(LocalDateTime.now().plusDays(1))
                .createdOn(LocalDateTime.now())
                .publishedOn(state == EventState.PUBLISHED ? LocalDateTime.now() : null)
                .category(createCategory())
                .initiator(createUser())
                .location(createLocation())
                .paid(true)
                .participantLimit(10L)
                .requestModeration(true)
                .eventState(state)
                .title(title)
                .build();
    }

    private NewEventDto createNewEventDto(LocalDateTime eventDate) {
        return NewEventDto.builder()
                .annotation("Test")
                .description("Test")
                .eventDate(eventDate)
                .category(1L)
                .location(createLocationDto())
                .paid(true)
                .participantLimit(10L)
                .requestModeration(true)
                .title("Test")
                .build();
    }

    private UpdateEventUserRequest createUpdateUserRequest(Long categoryId, LocalDateTime eventDate) {
        return UpdateEventUserRequest.builder()
                .annotation("Updated")
                .description("Updated")
                .eventDate(eventDate)
                .category(categoryId)
                .location(createLocationDto())
                .paid(false)
                .participantLimit(10L)
                .requestModeration(false)
                .stateAction(UserStateAction.SEND_TO_REVIEW)
                .title("Updated")
                .build();
    }

    private RequestsCountProjection createProjection(Long eventId, Long count) {
        return new RequestsCountProjection() {
            @Override
            public Long getEventId() {
                return eventId;
            }

            @Override
            public Long getCountRequests() {
                return count;
            }
        };
    }

    @Test
    void getEventsTest() {
        Event event1 = createEvent(1L, EventState.PUBLISHED, "test1");
        Event event2 = createEvent(2L, EventState.PUBLISHED, "test2");

        when(eventRepository.searchPublicEvents(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(SortEvent.VIEWS),
                eq(0L),
                eq(10L)
        )).thenReturn(List.of(event1, event2));

        when(participationRepository.countByEventIdsAndStatus(
                anyList(),
                eq(ParticipationStatus.CONFIRMED)
        )).thenReturn(List.of());

        when(eventViews.getView(List.of(1L, 2L)))
                .thenReturn(Map.of(
                        1L, 1L,
                        2L, 10L
                ));

        when(httpServletRequest.getRequestURI())
                .thenReturn("/events");

        when(httpServletRequest.getRemoteAddr())
                .thenReturn("127.0.0.1");

        List<EventShortDto> result = eventService.getEvents(
                null,
                null,
                null,
                null,
                null,
                null,
                SortEvent.VIEWS,
                0L,
                10L,
                httpServletRequest
        );

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).id());
        assertEquals(10L, result.get(0).views());
        assertEquals(1L, result.get(1).id());
        assertEquals(1L, result.get(1).views());

        ArgumentCaptor<EndpointHit> captor = ArgumentCaptor.forClass(EndpointHit.class);
        verify(statsClient).saveHit(captor.capture());

        EndpointHit hit = captor.getValue();

        assertEquals("ewm-main-service", hit.getApp());
        assertEquals("/events", hit.getUri());
        assertEquals("127.0.0.1", hit.getIp());
    }

    @Test
    void getEventsEndBeforeStartShouldReturnBadRequestException() {
        LocalDateTime rangeStart = LocalDateTime.now().plusDays(2);
        LocalDateTime rangeEnd = LocalDateTime.now().plusDays(1);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> eventService.getEvents(
                        null,
                        null,
                        null,
                        rangeStart,
                        rangeEnd,
                        null,
                        SortEvent.VIEWS,
                        0L,
                        10L,
                        httpServletRequest
                )
        );

        assertEquals("Дата начала позже даты конца", exception.getMessage());

    }

    @Test
    void getEventsAdminTest() {
        Event event = createEvent(EventState.PUBLISHED);

        when(eventRepository.searchAdminEvents(
                eq(List.of(1L)),
                eq(List.of(EventState.PUBLISHED)),
                eq(List.of(1L)),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(0L),
                eq(10L)
        )).thenReturn(List.of(event));

        when(participationRepository.countByEventIdsAndStatus(
                eq(List.of(1L)),
                eq(ParticipationStatus.CONFIRMED)
        )).thenReturn(List.of(createProjection(1L, 4L)));

        when(eventViews.getView(List.of(1L)))
                .thenReturn(Map.of(1L, 15L));

        List<EventFullDto> result = eventService.getEventsAdmin(
                List.of(1L),
                List.of(EventState.PUBLISHED),
                List.of(1L),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                0L,
                10L
        );

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().id());
        assertEquals(4L, result.getFirst().confirmedRequests());
        assertEquals(15L, result.getFirst().views());
    }

    @Test
    void getEventByIdTest() {
        Event event = createEvent(EventState.PUBLISHED);

        when(eventRepository.findByIdAndEventStateWithCategoryAndInitiator(
                1L,
                EventState.PUBLISHED
        )).thenReturn(Optional.of(event));

        when(participationRepository.countByEventIdsAndStatus(
                List.of(1L),
                ParticipationStatus.CONFIRMED
        )).thenReturn(List.of(createProjection(1L, 2L)));

        when(eventViews.getView(List.of(1L)))
                .thenReturn(Map.of(1L, 7L));

        when(httpServletRequest.getRequestURI())
                .thenReturn("/events/1");

        when(httpServletRequest.getRemoteAddr())
                .thenReturn("127.0.0.1");

        EventFullDto result = eventService.getEventById(1L, httpServletRequest);

        assertEquals(1L, result.id());
        assertEquals(2L, result.confirmedRequests());
        assertEquals(7L, result.views());

        verify(statsClient).saveHit(any(EndpointHit.class));
    }

    @Test
    void getEventsByIdInitiatorTest() {
        Event event = createEvent(EventState.PENDING);

        when(eventRepository.searchEventsByOwner(1L, 0L, 10L))
                .thenReturn(List.of(event));

        when(participationRepository.countByEventIdsAndStatus(
                List.of(1L),
                ParticipationStatus.CONFIRMED
        )).thenReturn(List.of(createProjection(1L, 1L)));

        when(eventViews.getView(List.of(1L)))
                .thenReturn(Map.of(1L, 5L));

        List<EventShortDto> result = eventService.getEventsByIdInitiator(1L, 0L, 10L);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().id());
        assertEquals(1L, result.getFirst().confirmedRequests());
        assertEquals(5L, result.getFirst().views());
    }

    @Test
    void createEventTest() {
        User user = createUser();
        Category category = createCategory();
        NewEventDto newEventDto = createNewEventDto(LocalDateTime.now().plusHours(3));
        Event savedEvent = createEvent(EventState.PENDING);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(eventRepository.save(any(Event.class)))
                .thenReturn(savedEvent);

        EventFullDto result = eventService.createEvent(1L, newEventDto);

        assertEquals(1L, result.id());
        assertEquals("Test", result.title());
        assertEquals(0L, result.confirmedRequests());
        assertEquals(0L, result.views());

        verify(userRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEventEventDateIsBefore2HoursShouldReturnBadRequestException() {
        NewEventDto newEventDto = createNewEventDto(LocalDateTime.now().plusHours(1));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(createUser()));

        assertThrows(BadRequestException.class, () -> eventService.createEvent(1L, newEventDto));

    }

    @Test
    void getEventByOwnerTest() {
        Event event = createEvent(EventState.PENDING);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(createUser()));

        when(eventRepository.findByIdAndEventInitiatorId(1L, 1L))
                .thenReturn(Optional.of(event));

        when(participationRepository.countByEventIdsAndStatus(
                List.of(1L),
                ParticipationStatus.CONFIRMED
        )).thenReturn(List.of(createProjection(1L, 2L)));

        when(eventViews.getView(List.of(1L)))
                .thenReturn(Map.of(1L, 6L));

        EventFullDto result = eventService.getEventByOwner(1L, 1L);

        assertEquals(1L, result.id());
        assertEquals(2L, result.confirmedRequests());
        assertEquals(6L, result.views());
    }

    @Test
    void updateEventByOwnerTest() {
        Event event = createEvent(EventState.PENDING);

        Category newCategory = Category.builder()
                .id(2L)
                .name("TestCategory")
                .build();

        UpdateEventUserRequest request = createUpdateUserRequest(
                2L,
                LocalDateTime.now().plusHours(3)
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(createUser()));

        when(eventRepository.findByIdAndEventInitiatorId(1L, 1L))
                .thenReturn(Optional.of(event));

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(newCategory));

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(participationRepository.countByEventIdsAndStatus(
                eq(List.of(1L)),
                eq(ParticipationStatus.CONFIRMED)
        )).thenReturn(List.of(createProjection(1L, 3L)));

        when(eventViews.getView(List.of(1L)))
                .thenReturn(Map.of(1L, 9L));

        EventFullDto result = eventService.updateEventByOwner(1L, 1L, request);

        assertEquals(1L, result.id());
        assertEquals(2L, result.category().id());
        assertEquals(3L, result.confirmedRequests());
        assertEquals(9L, result.views());

        verify(categoryRepository).findById(2L);
        verify(eventRepository).save(event);
    }

    @Test
    void updateEventByOwnerEventPublishedShouldConflictException() {
        Event event = createEvent(EventState.PUBLISHED);

        UpdateEventUserRequest request = createUpdateUserRequest(
                2L,
                LocalDateTime.now()
        );

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(createUser()));

        when(eventRepository.findByIdAndEventInitiatorId(anyLong(), anyLong()))
                .thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> eventService.updateEventByOwner(1L, 1L, request)
        );

        assertEquals("Только ожидающие события могут быть опубликованы", exception.getMessage());

    }

    @Test
    void updateEventByOwnerEventDateIsBefore2HoursShouldBadRequestException() {
        Event event = createEvent(EventState.PENDING);

        UpdateEventUserRequest request = createUpdateUserRequest(
                2L,
                LocalDateTime.now().plusHours(1)
        );

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(createUser()));

        when(eventRepository.findByIdAndEventInitiatorId(anyLong(), anyLong()))
                .thenReturn(Optional.of(event));

        assertThrows(BadRequestException.class, () -> eventService.updateEventByOwner(1L, 1L, request));

    }

    @Test
    void updateEventAdminTest() {
        Event event = createEvent(EventState.PENDING);

        Category category = Category.builder()
                .id(2L)
                .name("Test2")
                .build();

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .eventDate(LocalDateTime.now().plusHours(2))
                .category(2L)
                .build();

        when(eventRepository.findEventByIdWithCategoryAndInitiator(1L))
                .thenReturn(event);

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.ofNullable(category));

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(participationRepository.countByEventIdsAndStatus(
                List.of(1L),
                ParticipationStatus.CONFIRMED
        )).thenReturn(List.of(createProjection(1L, 0L)));

        when(eventViews.getView(List.of(1L)))
                .thenReturn(Map.of(1L, 0L));

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertEquals(1L, result.id());
        assertEquals(EventState.PUBLISHED, event.getEventState());
        assertNotNull(event.getPublishedOn());
    }

    @Test
    void updateEventAdminRejectedTest() {
        Event event = createEvent(EventState.PENDING);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.REJECT_EVENT)
                .eventDate(LocalDateTime.now().plusHours(2))
                .build();

        when(eventRepository.findEventByIdWithCategoryAndInitiator(1L))
                .thenReturn(event);

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(participationRepository.countByEventIdsAndStatus(
                List.of(1L),
                ParticipationStatus.CONFIRMED
        )).thenReturn(List.of(createProjection(1L, 0L)));

        when(eventViews.getView(List.of(1L)))
                .thenReturn(Map.of(1L, 0L));

        EventFullDto eventFullDto = eventService.updateEventAdmin(1L, request);

        assertEquals(1L, eventFullDto.id());
        assertEquals(EventState.CANCELED, event.getEventState());
    }

    @Test
    void updateEventAdminEventDateIsBefore1HourShouldBadRequestException() {
        Event event = createEvent(EventState.PENDING);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .eventDate(LocalDateTime.now().plusMinutes(30))
                .build();

        when(eventRepository.findEventByIdWithCategoryAndInitiator(anyLong()))
                .thenReturn(event);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> eventService.updateEventAdmin(1L, request));

        assertEquals("Дата события должна быть не позднее чем через 1 час после публикации", exception.getMessage());
    }

    @Test
    void updateEventAdminEventCategoryNotFoundException() {
        Event event = createEvent(EventState.PENDING);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .category(2L)
                .build();

        when(eventRepository.findEventByIdWithCategoryAndInitiator(anyLong()))
                .thenReturn(event);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.updateEventAdmin(1L, request));

        assertEquals("Категория с id=2 не найдена", exception.getMessage());
    }

    @Test
    void updateEventAdminEventNotPendingConflictException() {
        Event event = createEvent(EventState.CANCELED);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .build();

        when(eventRepository.findEventByIdWithCategoryAndInitiator(anyLong()))
                .thenReturn(event);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateEventAdmin(1L, request));

        assertEquals("Только ожидающие события события могут быть опубликованы", exception.getMessage());
    }

    @Test
    void updateEventAdminEventPublishCantCancelConflictException() {
        Event event = createEvent(EventState.PUBLISHED);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.REJECT_EVENT)
                .build();

        when(eventRepository.findEventByIdWithCategoryAndInitiator(anyLong()))
                .thenReturn(event);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateEventAdmin(1L, request));

        assertEquals("Опубликованное событие не может быть отменено", exception.getMessage());
    }


}