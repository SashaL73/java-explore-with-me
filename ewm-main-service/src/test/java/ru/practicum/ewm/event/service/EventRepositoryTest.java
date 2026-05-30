package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.config.QueryDslConfig;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.SortEvent;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.EventRepositoryCustomImpl;
import ru.practicum.ewm.participation.model.ParticipationRequest;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        EventRepositoryCustomImpl.class,
        QueryDslConfig.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EventRepositoryTest {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRepository participationRepository;
    private final TestEntityManager entityManager;

    private Category createCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }

    private User createUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }

    private Event createEvent(String annotation,
                              String description,
                              String title,
                              Category category,
                              User initiator,
                              EventState eventState,
                              LocalDateTime eventDate,
                              Boolean paid,
                              Long participantLimit) {
        return Event.builder()
                .annotation(annotation)
                .description(description)
                .eventDate(eventDate)
                .createdOn(LocalDateTime.now())
                .publishedOn(eventState == EventState.PUBLISHED ? LocalDateTime.now() : null)
                .category(category)
                .initiator(initiator)
                .location(Location.builder()
                        .lat(10.1F)
                        .lon(10.1F)
                        .build())
                .paid(paid)
                .participantLimit(participantLimit)
                .requestModeration(true)
                .eventState(eventState)
                .title(title)
                .build();
    }

    private ParticipationRequest createRequest(Event event,
                                               User requester,
                                               ParticipationStatus status) {
        return ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(status)
                .build();
    }

    @Test
    void searchPublicEventsFilterByTextCategoryPaidAndRangeTest() {
        User initiator = userRepository.save(createUser("Initiator", "initiator@test.com"));
        Category category1 = categoryRepository.save(createCategory("Category 1"));

        LocalDateTime eventDate = LocalDateTime.of(2030, 1, 10, 12, 0);
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 31, 23, 59);

        Event event = eventRepository.save(createEvent(
                "TestFind",
                "TestFind",
                "test1",
                category1,
                initiator,
                EventState.PUBLISHED,
                eventDate,
                true,
                10L
        ));

        eventRepository.save(createEvent(
                "Test",
                "Description",
                "test2",
                category1,
                initiator,
                EventState.PUBLISHED,
                eventDate,
                true,
                10L
        ));

        List<Event> eventList = eventRepository.searchPublicEvents(
                "TestFind",
                List.of(category1.getId()),
                true,
                start,
                end,
                false,
                SortEvent.EVENT_DATE,
                0L,
                10L
        );

        assertEquals(1, eventList.size());
        assertEquals(event.getId(), eventList.getFirst().getId());
        assertEquals("test1", eventList.getFirst().getTitle());
        assertEquals(EventState.PUBLISHED, eventList.getFirst().getEventState());
    }

    @Test
    void searchPublicEventsBlankTextAndEmptyCategoriesTest() {
        User initiator = userRepository.save(createUser("Initiator", "initiator@test.com"));
        Category category = categoryRepository.save(createCategory("Category"));

        Event event = eventRepository.save(createEvent(
                "Test",
                "Test",
                "Test",
                category,
                initiator,
                EventState.PUBLISHED,
                LocalDateTime.of(2030, 1, 10, 12, 0),
                true,
                10L
        ));

        List<Event> result = eventRepository.searchPublicEvents(
                "   ",
                List.of(),
                null,
                null,
                null,
                null,
                SortEvent.EVENT_DATE,
                0L,
                10L
        );

        assertEquals(1, result.size());
        assertEquals(event.getId(), result.getFirst().getId());
    }

    @Test
    void searchPublicEventsWhenOnlyAvailableFullEventsTest() {
        User initiator = userRepository.save(createUser("Initiator", "initiator@test.com"));
        User requester = userRepository.save(createUser("Requester", "requester@test.com"));
        Category category = categoryRepository.save(createCategory("Category"));

        Event eventLimit0 = eventRepository.save(createEvent(
                "Test1",
                "Test1",
                "Test1",
                category,
                initiator,
                EventState.PUBLISHED,
                LocalDateTime.of(2030, 1, 10, 12, 0),
                true,
                0L
        ));

        Event eventFree = eventRepository.save(createEvent(
                "Test2",
                "Test2",
                "Test2",
                category,
                initiator,
                EventState.PUBLISHED,
                LocalDateTime.of(2030, 1, 11, 12, 0),
                true,
                2L
        ));

        Event fullEvent = eventRepository.save(createEvent(
                "Test3",
                "Test3",
                "Test3",
                category,
                initiator,
                EventState.PUBLISHED,
                LocalDateTime.of(2030, 1, 12, 12, 0),
                true,
                1L
        ));

        participationRepository.save(createRequest(eventLimit0, requester, ParticipationStatus.CONFIRMED));
        participationRepository.save(createRequest(eventFree, requester, ParticipationStatus.CONFIRMED));
        participationRepository.save(createRequest(fullEvent, requester, ParticipationStatus.CONFIRMED));

        List<Event> result = eventRepository.searchPublicEvents(
                null,
                null,
                null,
                null,
                null,
                true,
                SortEvent.EVENT_DATE,
                0L,
                10L
        );

        List<Long> eventIds = result.stream()
                .map(Event::getId)
                .toList();

        assertEquals(2, result.size());
        assertTrue(eventIds.contains(eventLimit0.getId()));
        assertTrue(eventIds.contains(eventFree.getId()));
        assertFalse(eventIds.contains(fullEvent.getId()));
    }

    @Test
    void searchAdminEventsFilterByUsersStatesCategoriesAndRangeTest() {
        User user1 = userRepository.save(createUser("User 1", "user1@test.com"));
        User user2 = userRepository.save(createUser("User 2", "user2@test.com"));

        Category category1 = categoryRepository.save(createCategory("Category 1"));

        LocalDateTime targetDate = LocalDateTime.of(2030, 1, 10, 12, 0);
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 31, 23, 59);

        Event target = eventRepository.save(createEvent(
                "Test1",
                "Test1",
                "Test1",
                category1,
                user1,
                EventState.PUBLISHED,
                targetDate,
                true,
                10L
        ));

        eventRepository.save(createEvent(
                "Test2",
                "Test2",
                "Test2",
                category1,
                user2,
                EventState.PUBLISHED,
                targetDate,
                true,
                10L
        ));

        List<Event> result = eventRepository.searchAdminEvents(
                List.of(user1.getId()),
                List.of(EventState.PUBLISHED),
                List.of(category1.getId()),
                start,
                end,
                0L,
                10L
        );

        assertEquals(1, result.size());
        assertEquals(target.getId(), result.getFirst().getId());
    }

    @Test
    void searchAdminEventsWhenFiltersAreNullShouldReturnAllEventsTest() {
        User user = userRepository.save(createUser("User", "user@test.com"));
        Category category = categoryRepository.save(createCategory("Category"));

        Event event1 = eventRepository.save(createEvent(
                "Test1",
                "Test1",
                "Test1",
                category,
                user,
                EventState.PUBLISHED,
                LocalDateTime.of(2030, 1, 1, 12, 0),
                true,
                10L
        ));

        Event event2 = eventRepository.save(createEvent(
                "Test2",
                "Test2",
                "Test2",
                category,
                user,
                EventState.PENDING,
                LocalDateTime.of(2030, 1, 2, 12, 0),
                true,
                10L
        ));

        List<Event> result = eventRepository.searchAdminEvents(
                null,
                null,
                null,
                null,
                null,
                0L,
                10L
        );

        List<Long> ids = result.stream()
                .map(Event::getId)
                .toList();

        assertEquals(2, result.size());
        assertTrue(ids.contains(event1.getId()));
        assertTrue(ids.contains(event2.getId()));
    }

    @Test
    void searchAdminEventsWhenFiltersAreEmptyShouldIgnoreThemTest() {
        User user = userRepository.save(createUser("User", "user@test.com"));
        Category category = categoryRepository.save(createCategory("Category"));

        eventRepository.save(createEvent(
                "Test1",
                "Test1",
                "Test1",
                category,
                user,
                EventState.PUBLISHED,
                LocalDateTime.of(2030, 1, 1, 12, 0),
                true,
                10L
        ));

        eventRepository.save(createEvent(
                "Test2",
                "Test2",
                "Test2",
                category,
                user,
                EventState.CANCELED,
                LocalDateTime.of(2030, 1, 2, 12, 0),
                true,
                10L
        ));

        List<Event> result = eventRepository.searchAdminEvents(
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                0L,
                10L
        );

        assertEquals(2, result.size());
    }

}