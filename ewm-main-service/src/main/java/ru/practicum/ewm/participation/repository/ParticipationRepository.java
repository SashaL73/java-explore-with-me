package ru.practicum.ewm.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.participation.model.ParticipationRequest;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.repository.projection.RequestsCountProjection;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<ParticipationRequest, Long> {

    Long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByEventIdAndEventInitiatorIdOrderByIdAsc(Long eventId,
                                                                               Long initiatorId);

    List<ParticipationRequest> findAllByEventIdAndStatusOrderByIdAsc(Long eventId,
                                                                     ParticipationStatus status);

    List<ParticipationRequest> findAllByEventIdAndEventInitiatorIdAndIdInOrderByIdAsc(Long eventId,
                                                                                      Long initiatorId,
                                                                                      List<Long> requestsIds);

    @Query("select r.event.id as eventId, " +
            "count(r.id) as countRequests " +
            "from ParticipationRequest r " +
            "where r.event.id in :eventIds " +
            "and r.status = :status " +
            "group by r.event.id")
    List<RequestsCountProjection> countByEventIdsAndStatus(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") ParticipationStatus status
    );

}
