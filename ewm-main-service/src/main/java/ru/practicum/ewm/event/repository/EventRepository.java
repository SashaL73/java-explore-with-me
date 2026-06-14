package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {

    boolean existsByCategoryId(Long categoryId);

    @Query("select e from Event e " +
            "join fetch e.category " +
            "join fetch e.initiator " +
            "where e.id = :id")
    Event findEventByIdWithCategoryAndInitiator(Long id);

    @Query("select e from Event e " +
            "join fetch e.category " +
            "join fetch e.initiator " +
            "where e.id = :id " +
            "and e.eventState = :state")
    Optional<Event> findByIdAndEventStateWithCategoryAndInitiator(@Param("id") Long id,
                                                                  @Param("state") EventState eventState);

    @Query("select e from Event e " +
            "join fetch e.category " +
            "join fetch e.initiator " +
            "where e.id = :id " +
            "and e.initiator.id = :initId")
    Optional<Event> findByIdAndEventInitiatorId(@Param("id") Long eventId, @Param("initId") Long initiatorId);

    @Query("select e from Event e " +
            "join fetch e.category " +
            "join fetch e.initiator " +
            "where e.initiator.id in " +
            "(select us.targetUser.id from UserSubscription us " +
            "where us.subscriber.id = :subscriberId) " +
            "and e.eventState = :state")
    List<Event> findSubscribedUsersEvents(@Param("subscriberId") Long subscriberId, @Param("state") EventState state);
}
