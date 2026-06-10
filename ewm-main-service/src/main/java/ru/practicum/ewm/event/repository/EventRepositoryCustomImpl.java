package ru.practicum.ewm.event.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.category.model.QCategory;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.model.SortEvent;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.model.QParticipationRequest;
import ru.practicum.ewm.user.model.QUser;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public List<Event> searchPublicEvents(String text,
                                          List<Long> categories,
                                          Boolean paid,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd,
                                          Boolean onlyAvailable,
                                          SortEvent sort,
                                          Long from,
                                          Long size) {

        QEvent event = QEvent.event;
        QCategory category = QCategory.category;
        QUser initiator = QUser.user;
        QParticipationRequest request = QParticipationRequest.participationRequest;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.and(event.eventState.eq(EventState.PUBLISHED));

        if (text != null && !text.isBlank()) {
            booleanBuilder.and(event.annotation.containsIgnoreCase(text).or(event.description.containsIgnoreCase(text)));
        }

        if (categories != null && !categories.isEmpty()) {
            booleanBuilder.and(event.category.id.in(categories));
        }

        if (paid != null) {
            booleanBuilder.and(event.paid.eq(paid));
        }

        if (rangeStart != null) {
            booleanBuilder.and(event.eventDate.goe(rangeStart));
        }

        if (rangeEnd != null) {
            booleanBuilder.and(event.eventDate.loe(rangeEnd));
        }

        if (Boolean.TRUE.equals(onlyAvailable)) {
            JPQLQuery<Long> confirmedRequests = JPAExpressions
                    .select(request.id.count())
                    .from(request)
                    .where(request.event.id.eq(event.id).and(request.status.eq(ParticipationStatus.CONFIRMED)));

            booleanBuilder.and(
                    event.participantLimit.eq(0L)
                            .or(event.participantLimit.longValue().gt(confirmedRequests))
            );

        }

        return jpaQueryFactory.selectFrom(event)
                .leftJoin(event.category, category).fetchJoin()
                .leftJoin(event.initiator, initiator).fetchJoin()
                .where(booleanBuilder)
                .orderBy(orderSpecifier(event, sort))
                .offset(from)
                .limit(size)
                .fetch();
    }

    @Override
    public List<Event> searchAdminEvents(List<Long> users,
                                         List<EventState> eventStates,
                                         List<Long> categories,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Long from,
                                         Long size) {

        QEvent event = QEvent.event;
        QCategory category = QCategory.category;
        QUser initiator = QUser.user;

        BooleanBuilder booleanBuilder = new BooleanBuilder();


        if (users != null && !users.isEmpty()) {
            booleanBuilder.and(event.initiator.id.in(users));
        }

        if (eventStates != null && !eventStates.isEmpty()) {
            booleanBuilder.and(event.eventState.in(eventStates));
        }

        if (categories != null && !categories.isEmpty()) {
            booleanBuilder.and(event.category.id.in(categories));
        }

        if (rangeStart != null) {
            booleanBuilder.and(event.eventDate.goe(rangeStart));
        }

        if (rangeEnd != null) {
            booleanBuilder.and(event.eventDate.loe(rangeEnd));
        }

        return jpaQueryFactory.selectFrom(event)
                .leftJoin(event.category, category).fetchJoin()
                .leftJoin(event.initiator, initiator).fetchJoin()
                .where(booleanBuilder)
                .offset(from)
                .limit(size)
                .fetch();
    }

    @Override
    public List<Event> searchEventsByOwner(Long initiatorId, Long from, Long size) {
        QEvent event = QEvent.event;
        QCategory category = QCategory.category;
        QUser initiator = QUser.user;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.and(event.initiator.id.eq(initiatorId));


        return jpaQueryFactory.selectFrom(event)
                .leftJoin(event.category, category).fetchJoin()
                .leftJoin(event.initiator, initiator).fetchJoin()
                .where(booleanBuilder)
                .offset(from)
                .limit(size)
                .fetch();
    }

    private OrderSpecifier<?> orderSpecifier(QEvent event, SortEvent sort) {

        if (sort == null) {
            return event.eventDate.asc();
        }

        return event.eventDate.asc();
    }

}
