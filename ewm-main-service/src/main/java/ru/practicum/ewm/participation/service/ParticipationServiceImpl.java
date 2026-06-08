package ru.practicum.ewm.participation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.participation.model.ParticipationRequest;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> getParticipation(Long userId) {
        log.info("Получение списка заявок пользователя id={}", userId);
        findUserOrElseThrow(userId);
        return participationRepository.findByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .toList();

    }

    @Override
    @Transactional
    public ParticipationRequestDto createParticipation(Long userId, Long eventId) {
        log.info("Создание заявки на событие eventId={} пользователем id={}", eventId, userId);
        User user = findUserOrElseThrow(userId);
        Event event = findEventOrElseThrow(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор не может оставлять заявки на событие");
        }

        if (!event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие с id=" + eventId + " не опубликовано");
        }

        if (participationRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Заявка уже существет");
        }

        long confirmedRequests = participationRepository.countByEventIdAndStatus(
                eventId,
                ParticipationStatus.CONFIRMED
        );

        if (event.getParticipantLimit() != 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников исчерпан");
        }

        ParticipationStatus status = event.getParticipantLimit() == 0 || !event.getRequestModeration()
                ? ParticipationStatus.CONFIRMED
                : ParticipationStatus.PENDING;

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .status(status)
                .build();

        participationRepository.save(participationRequest);

        return ParticipationRequestMapper.mapToParticipationRequestDto(participationRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipation(Long userId, Long requestId) {
        log.info("Отмена заявки requestId={} пользователем id={}", requestId, userId);
        findUserOrElseThrow(userId);
        ParticipationRequest participationRequest = participationRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с id=" + requestId + " не найдена"));

        if (!participationRequest.getRequester().getId().equals(userId)) {
            throw new ConflictException("Пользователь id=" + userId + " не является владельцем заявки");
        }

        participationRequest.setStatus(ParticipationStatus.CANCELED);
        return ParticipationRequestMapper.mapToParticipationRequestDto(participationRequest);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByOwnerEvent(Long userId, Long eventId) {
        log.info("Получение заявки eventId={} пользователя id={}", eventId, userId);
        List<ParticipationRequest> participationRequests = participationRepository
                .findAllByEventIdAndEventInitiatorIdOrderByIdAsc(eventId, userId);

        return participationRequests.stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatusRequestsByOwnerEvent(
            Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        log.info("Обновление статуса заявки eventId={}, пользователем id={}, request={}", eventId, userId, request);
        Event event = findEventOrElseThrow(eventId);

        List<ParticipationRequest> participationRequests = participationRepository
                .findAllByEventIdAndEventInitiatorIdAndIdInOrderByIdAsc(eventId, userId, request.requestIds());

        if (participationRequests.size() != request.requestIds().stream().distinct().count()) {
            throw new NotFoundException("Не все заявки найдены");
        }

        boolean hasNotPendingRequest = participationRequests.stream()
                .anyMatch(participationRequest ->
                        participationRequest.getStatus() != ParticipationStatus.PENDING
                );

        if (hasNotPendingRequest) {
            throw new ConflictException("Только ожидающим заявка можно менять статус");
        }

        if (request.status() == ParticipationStatus.REJECTED) {
            participationRequests.forEach(participationRequest ->
                    participationRequest.setStatus(ParticipationStatus.REJECTED)
            );

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(participationRequests.stream()
                            .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                            .toList())
                    .build();
        }

        if (request.status() == ParticipationStatus.CONFIRMED) {
            return confirmRequests(event, participationRequests);
        }

        return null;
    }

    private EventRequestStatusUpdateResult confirmRequests(Event event,
                                                           List<ParticipationRequest> requests) {
        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        Long participantLimit = event.getParticipantLimit();

        if (participantLimit == 0) {
            requests.forEach(request -> request.setStatus(ParticipationStatus.CONFIRMED));
            confirmedRequests.addAll(requests);

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(confirmedRequests.stream()
                            .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                            .toList())
                    .rejectedRequests(List.of())
                    .build();
        }

        Long confirmedCount = participationRepository.countByEventIdAndStatus(
                event.getId(),
                ParticipationStatus.CONFIRMED
        );

        if (confirmedCount >= participantLimit) {
            throw new ConflictException("Превышен лимит заявок");
        }

        for (ParticipationRequest request : requests) {
            if (confirmedCount < participantLimit) {
                request.setStatus(ParticipationStatus.CONFIRMED);
                confirmedRequests.add(request);
                confirmedCount++;
            }
        }

        if (confirmedCount.equals(participantLimit)) {
            List<ParticipationRequest> pendingRequests =
                    participationRepository.findAllByEventIdAndStatusOrderByIdAsc(
                            event.getId(),
                            ParticipationStatus.PENDING
                    );

            pendingRequests.forEach(request -> request.setStatus(ParticipationStatus.REJECTED));
            rejectedRequests.addAll(pendingRequests);
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests.stream()
                        .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                        .toList())
                .rejectedRequests(rejectedRequests.stream()
                        .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                        .toList())
                .build();
    }


    private Event findEventOrElseThrow(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие id=" + eventId + " не найдено")
        );
    }

    private User findUserOrElseThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь id=" + userId + " не найден")
        );
    }
}
