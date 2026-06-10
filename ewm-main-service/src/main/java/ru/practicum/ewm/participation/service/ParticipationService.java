package ru.practicum.ewm.participation.service;

import ru.practicum.ewm.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationService {
    List<ParticipationRequestDto> getParticipation(Long userId);

    ParticipationRequestDto createParticipation(Long userId, Long eventId);

    ParticipationRequestDto cancelParticipation(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequestsByOwnerEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequestsByOwnerEvent(Long userId,
                                                                    Long eventId,
                                                                    EventRequestStatusUpdateRequest request);

}
