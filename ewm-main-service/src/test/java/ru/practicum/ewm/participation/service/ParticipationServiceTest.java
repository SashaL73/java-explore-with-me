package ru.practicum.ewm.participation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.model.ParticipationRequest;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ParticipationServiceTest {

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ParticipationServiceImpl participationService;

    private final Category category = Category.builder()
            .name("Test")
            .build();

    private final User userInitiator = User.builder()
            .id(1L)
            .name("Test")
            .email("test@test.com")
            .build();

    private final User userRequester = User.builder()
            .id(2L)
            .name("Test1")
            .email("test@test1.com")
            .build();

    private Event getEvent(EventState state, Long limit, boolean moderation) {
        return Event.builder()
                .id(1L)
                .createdOn(LocalDateTime.of(2026, 6, 4, 1, 1))
                .annotation("TestAnnotation")
                .category(category)
                .description("TestDescription")
                .eventDate(LocalDateTime.now().plusHours(2))
                .initiator(userInitiator)
                .location(Location.builder()
                        .lat(10.1F)
                        .lon(10.1F)
                        .build())
                .paid(true)
                .participantLimit(limit)
                .requestModeration(moderation)
                .eventState(state)
                .title("TestTitle")
                .build();

    }

    private final ParticipationRequest participationRequest = ParticipationRequest.builder()
            .id(1L)
            .created(LocalDateTime.of(2026, 6, 6, 1, 1))
            .event(getEvent(EventState.PUBLISHED, 0L, true))
            .requester(userRequester)
            .status(ParticipationStatus.PENDING)
            .build();

    private ParticipationRequest getParticipationRequest(Long id, ParticipationStatus status) {
        return ParticipationRequest.builder()
                .id(id)
                .created(LocalDateTime.of(2026, 6, 6, 1, 1))
                .event(getEvent(EventState.PUBLISHED, 0L, true))
                .requester(userRequester)
                .status(status)
                .build();
    }

    @Test
    void getParticipationTest() {
        List<ParticipationRequest> participationRequests = List.of(participationRequest);

        when(participationRepository.findByRequesterId(Mockito.anyLong()))
                .thenReturn(participationRequests);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userInitiator));

        List<ParticipationRequestDto> requestDtos = participationService.getParticipation(1L);

        assertEquals(1, requestDtos.size());
    }

    @Test
    void createParticipationLimit0Test() {
        Event event = getEvent(EventState.PUBLISHED, 0L, true);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userRequester));

        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(event));

        when(participationRepository.save(Mockito.any(ParticipationRequest.class)))
                .thenReturn(participationRequest);

        ParticipationRequestDto requestDto = participationService.createParticipation(2L, 1L);

        assertEquals(ParticipationStatus.CONFIRMED, requestDto.status());
        assertEquals(2L, requestDto.requester());
    }

    @Test
    void createParticipationLimit0RequestModerationFalse() {
        Event event = getEvent(EventState.PUBLISHED, 1L, false);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userRequester));

        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(event));

        when(participationRepository.save(Mockito.any(ParticipationRequest.class)))
                .thenReturn(participationRequest);

        ParticipationRequestDto requestDto = participationService.createParticipation(2L, 1L);

        assertEquals(ParticipationStatus.CONFIRMED, requestDto.status());
        assertEquals(2L, requestDto.requester());
    }

    @Test
    void createParticipationRequestModerationFalseTest() {
        Event event = getEvent(EventState.PUBLISHED, 0L, false);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userRequester));

        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(event));

        ParticipationRequestDto requestDto = participationService.createParticipation(2L, 1L);

        assertEquals(ParticipationStatus.CONFIRMED, requestDto.status());
        assertEquals(2L, requestDto.requester());

    }

    @Test
    void createParticipationRequestModerationTrueLimit1Test() {
        Event event = getEvent(EventState.PUBLISHED, 1L, true);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userRequester));

        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(event));

        ParticipationRequestDto requestDto = participationService.createParticipation(2L, 1L);

        assertEquals(ParticipationStatus.PENDING, requestDto.status());
        assertEquals(2L, requestDto.requester());
    }


    @Test
    void createParticipationEventOwnerShouldReturnConflictException() {
        Event event = getEvent(EventState.PUBLISHED, 0L, true);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userInitiator));

        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(event));

        ConflictException exception = assertThrows(ConflictException.class, () -> participationService
                .createParticipation(1L, 1L));

        assertEquals("Инициатор не может оставлять заявки на событие", exception.getMessage());
    }


    @Test
    void createParticipationNotPublishedEventShouldReturnConflictException() {
        Event event = getEvent(EventState.PENDING, 0L, true);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userRequester));

        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(event));

        ConflictException exception = assertThrows(ConflictException.class, () -> participationService
                .createParticipation(2L, 2L));

        assertEquals("Событие с id=2 не опубликовано", exception.getMessage());

    }

    @Test
    void createParticipationExistParticipationByRequesterEventShouldReturnConflictException() {
        Event event = getEvent(EventState.PUBLISHED, 0L, true);
        when(participationRepository.existsByRequesterIdAndEventId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(true);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userRequester));

        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(event));

        ConflictException exception = assertThrows(ConflictException.class, () -> participationService
                .createParticipation(2L, 1L));

        assertEquals("Заявка уже существет", exception.getMessage());
    }

    @Test
    void createParticipationFullLimitEventShouldReturnConflictException() {
        Event event = getEvent(EventState.PUBLISHED, 1L, true);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userRequester));

        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(event));

        when(participationRepository.countByEventIdAndStatus(
                Mockito.anyLong(), Mockito.any(ParticipationStatus.class)
        )).thenReturn(1L);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationService.createParticipation(2L, 3L));

        assertEquals("Лимит участников исчерпан", exception.getMessage());
    }

    @Test
    void cancelParticipationTest() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userRequester));

        when(participationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(participationRequest));

        ParticipationRequestDto requestDto = participationService.cancelParticipation(2L, 1L);
        assertEquals(ParticipationStatus.CANCELED, requestDto.status());
    }

    @Test
    void cancelParticipationNotOwnerParticipationShouldReturnConflictExceptionTest() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userInitiator));

        when(participationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(participationRequest));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> participationService.cancelParticipation(1L, 1L)
        );

        assertEquals("Пользователь id=1 не является владельцем заявки", exception.getMessage());

    }

    @Test
    void cancelParticipationNotExistParticipationShouldReturnNotFoundExceptionTest() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(userRequester));

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> participationService.cancelParticipation(2L, 999L)
        );

        assertEquals("Заявка с id=999 не найдена", exception.getMessage());
    }

    @Test
    void cancelParticipationNotExistRequesterShouldReturnNotFoundExceptionTest() {
        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> participationService.cancelParticipation(999L, 1L)
        );

        assertEquals("Пользователь id=999 не найден", exception.getMessage());
    }

    @Test
    void getRequestsByOwnerEventTest() {
        List<ParticipationRequest> requests = List.of(participationRequest);

        when(participationRepository
                .findAllByEventIdAndEventInitiatorIdOrderByIdAsc(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(requests);

        List<ParticipationRequestDto> requestDtos = participationService.getRequestsByOwnerEvent(1L, 1L);

        assertEquals(1, requestDtos.size());
        assertEquals(participationRequest.getEvent().getId(), requests.getFirst().getEvent().getId());
    }

    @Test
    void updateStatusRequestsOnRejectedByOwnerEventTest() {
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L))
                .status(ParticipationStatus.REJECTED)
                .build();
        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(participationRequest.getEvent()));
        List<ParticipationRequest> requests = List.of(participationRequest);
        when(participationRepository
                .findAllByEventIdAndEventInitiatorIdAndIdInOrderByIdAsc(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyList()))
                .thenReturn(requests);

        EventRequestStatusUpdateResult requestResult = participationService
                .updateStatusRequestsByOwnerEvent(1L, 1L, request);

        List<ParticipationRequestDto> confirmedRequests = requestResult.confirmedRequests();
        List<ParticipationRequestDto> rejectedRequests = requestResult.rejectedRequests();

        assertEquals(0, confirmedRequests.size());
        assertEquals(1, rejectedRequests.size());

    }

    @Test
    void updateStatusRequestsByOwnerEventNotFoundException() {
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L))
                .status(ParticipationStatus.REJECTED)
                .build();
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participationService.updateStatusRequestsByOwnerEvent(1L, 2L, request));

        assertEquals("Событие id=2 не найдено", exception.getMessage());
    }

    @Test
    void updateStatusRequestsByOwnerRequestsNotFoundException() {
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L))
                .status(ParticipationStatus.REJECTED)
                .build();

        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(participationRequest.getEvent()));
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participationService.updateStatusRequestsByOwnerEvent(1L, 1L, request));

        assertEquals("Не все заявки найдены", exception.getMessage());
    }

    @Test
    void updateStatusRequestsByOwnerEventNotPendingShouldReturnConflictExceptionTest() {
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L, 2L))
                .status(ParticipationStatus.REJECTED)
                .build();
        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(participationRequest.getEvent()));

        List<ParticipationRequest> requests = List.of(getParticipationRequest(1L, ParticipationStatus.PENDING),
                getParticipationRequest(2L, ParticipationStatus.CANCELED));
        when(participationRepository
                .findAllByEventIdAndEventInitiatorIdAndIdInOrderByIdAsc(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyList()))
                .thenReturn(requests);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationService.updateStatusRequestsByOwnerEvent(1L, 1L, request));

        assertEquals("Только ожидающим заявка можно менять статус", exception.getMessage());

    }

    @Test
    void updateStatusRequestsOnConfirmedByOwnerEventTest() {
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L))
                .status(ParticipationStatus.CONFIRMED)
                .build();
        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(participationRequest.getEvent()));
        List<ParticipationRequest> requests = List.of(participationRequest);
        when(participationRepository
                .findAllByEventIdAndEventInitiatorIdAndIdInOrderByIdAsc(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyList()))
                .thenReturn(requests);

        EventRequestStatusUpdateResult requestResult = participationService
                .updateStatusRequestsByOwnerEvent(1L, 1L, request);

        List<ParticipationRequestDto> confirmedRequests = requestResult.confirmedRequests();
        List<ParticipationRequestDto> rejectedRequests = requestResult.rejectedRequests();

        assertEquals(1, confirmedRequests.size());
        assertEquals(0, rejectedRequests.size());
    }

    @Test
    void updateStatusRequestsOnConfirmedButFullLimitEventTestShouldReturnConflictException() {
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L))
                .status(ParticipationStatus.CONFIRMED)
                .build();
        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(getEvent(EventState.PENDING, 1L, true)));
        List<ParticipationRequest> requests = List.of(participationRequest);
        when(participationRepository
                .findAllByEventIdAndEventInitiatorIdAndIdInOrderByIdAsc(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyList()))
                .thenReturn(requests);
        when(participationRepository.countByEventIdAndStatus(
                Mockito.anyLong(),
                Mockito.any(ParticipationStatus.class)))
                .thenReturn(1L);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationService.updateStatusRequestsByOwnerEvent(1L, 1L, request));

        assertEquals("Превышен лимит заявок", exception.getMessage());
    }

    @Test
    void updateStatusRequestsOnConfirmedLimit2Test() {
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L, 2L, 3L))
                .status(ParticipationStatus.CONFIRMED)
                .build();
        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(getEvent(EventState.PUBLISHED, 2L, true)));
        List<ParticipationRequest> requests = List.of(getParticipationRequest(1L, ParticipationStatus.PENDING),
                getParticipationRequest(2L, ParticipationStatus.PENDING),
                getParticipationRequest(3L, ParticipationStatus.PENDING));
        when(participationRepository
                .findAllByEventIdAndEventInitiatorIdAndIdInOrderByIdAsc(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyList()))
                .thenReturn(requests);
        when(participationRepository.countByEventIdAndStatus(
                Mockito.anyLong(),
                Mockito.any(ParticipationStatus.class)))
                .thenReturn(0L);

        when(participationRepository.findAllByEventIdAndStatusOrderByIdAsc(
                Mockito.anyLong(),
                Mockito.any(ParticipationStatus.class)))
                .thenReturn(List.of(requests.get(2)));

        EventRequestStatusUpdateResult requestResult = participationService
                .updateStatusRequestsByOwnerEvent(1L, 1L, request);

        List<ParticipationRequestDto> confirmedRequests = requestResult.confirmedRequests();
        List<ParticipationRequestDto> rejectedRequests = requestResult.rejectedRequests();

        assertEquals(2, confirmedRequests.size());
        assertEquals(1, rejectedRequests.size());

        assertEquals(ParticipationStatus.CONFIRMED, requests.get(0).getStatus());
        assertEquals(ParticipationStatus.CONFIRMED, requests.get(1).getStatus());
        assertEquals(ParticipationStatus.REJECTED, requests.get(2).getStatus());
    }

    @Test
    void updateStatusRequestsOnConfirmedLimit2ExistConfirmedRequests1Test() {
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L, 2L, 3L))
                .status(ParticipationStatus.CONFIRMED)
                .build();

        when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(getEvent(EventState.PUBLISHED, 2L, true)));

        List<ParticipationRequest> requests = List.of(
                getParticipationRequest(1L, ParticipationStatus.PENDING),
                getParticipationRequest(2L, ParticipationStatus.PENDING),
                getParticipationRequest(3L, ParticipationStatus.PENDING)
        );

        when(participationRepository
                .findAllByEventIdAndEventInitiatorIdAndIdInOrderByIdAsc(
                        Mockito.anyLong(),
                        Mockito.anyLong(),
                        Mockito.anyList()))
                .thenReturn(requests);

        when(participationRepository.countByEventIdAndStatus(
                Mockito.anyLong(),
                Mockito.eq(ParticipationStatus.CONFIRMED)))
                .thenReturn(1L);

        when(participationRepository.findAllByEventIdAndStatusOrderByIdAsc(
                Mockito.anyLong(),
                Mockito.eq(ParticipationStatus.PENDING)))
                .thenReturn(List.of(requests.get(1), requests.get(2)));

        EventRequestStatusUpdateResult requestResult = participationService
                .updateStatusRequestsByOwnerEvent(1L, 1L, request);

        List<ParticipationRequestDto> confirmedRequests = requestResult.confirmedRequests();
        List<ParticipationRequestDto> rejectedRequests = requestResult.rejectedRequests();

        assertEquals(1, confirmedRequests.size());
        assertEquals(2, rejectedRequests.size());

        assertEquals(ParticipationStatus.CONFIRMED, requests.get(0).getStatus());
        assertEquals(ParticipationStatus.REJECTED, requests.get(1).getStatus());
        assertEquals(ParticipationStatus.REJECTED, requests.get(2).getStatus());
    }


}
