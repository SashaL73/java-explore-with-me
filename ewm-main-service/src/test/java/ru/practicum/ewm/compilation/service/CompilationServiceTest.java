package ru.practicum.ewm.compilation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.service.view.EventViews;
import ru.practicum.ewm.participation.model.ParticipationStatus;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.participation.repository.projection.RequestsCountProjection;
import ru.practicum.ewm.user.model.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CompilationServiceTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private EventViews eventViews;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private final Event event = Event.builder()
            .id(1L)
            .createdOn(LocalDateTime.of(2026, 6, 4, 1, 1))
            .annotation("TestAnnotation")
            .category(Category.builder()
                    .id(1L)
                    .name("Test")
                    .build())
            .description("TestDescription")
            .eventDate(LocalDateTime.now().plusHours(2))
            .initiator(User.builder()
                    .id(1L)
                    .email("test@test.com")
                    .name("Test")
                    .build())
            .location(Location.builder()
                    .lat(10.1F)
                    .lon(10.1F)
                    .build())
            .paid(true)
            .participantLimit(0L)
            .requestModeration(true)
            .eventState(EventState.PUBLISHED)
            .title("TestTitle")
            .build();

    private Compilation createCompilation(List<Event> events) {
        return Compilation.builder()
                .id(1L)
                .title("Test")
                .pinned(true)
                .events(events)
                .build();
    }

    private NewCompilationDto createNewCompilationDto(List<Long> events) {
        return NewCompilationDto.builder()
                .events(events)
                .pinned(true)
                .title("Test")
                .build();
    }


    @Test
    void getCompilationTest() {
        when(compilationRepository.findByIdWithEvents(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(createCompilation(List.of(event))));

        CompilationDto compilationDto = compilationService.getCompilation(1L);

        assertEquals(1, compilationDto.id());
        assertEquals(1, compilationDto.events().size());
        assertEquals(true, compilationDto.pinned());
        assertEquals(1, compilationDto.events().getFirst().id());

    }

    @Test
    void getCompilationNotExistIdShouldReturnNotFoundExceptionTest() {

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> compilationService.getCompilation(1L));

        assertEquals("Подборка событий с id=1 не найдена", exception.getMessage());
    }

    @Test
    void createCompilationWithEventsTest() {
        Compilation compilation = createCompilation(List.of(event));
        NewCompilationDto newCompilationDto = createNewCompilationDto(List.of(1L));

        RequestsCountProjection projection = Mockito.mock(RequestsCountProjection.class);

        when(projection.getEventId()).thenReturn(1L);
        when(projection.getCountRequests()).thenReturn(5L);

        when(eventRepository.findAllById(Mockito.anyList()))
                .thenReturn(List.of(event));

        when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenReturn(compilation);

        when(participationRepository.countByEventIdsAndStatus(
                Mockito.anyList(),
                Mockito.eq(ParticipationStatus.CONFIRMED)))
                .thenReturn(List.of(projection));

        when(eventViews.getView(Mockito.anyList()))
                .thenReturn(Map.of(1L, 10L));

        CompilationDto compilationDto = compilationService.createCompilation(newCompilationDto);

        assertEquals(1L, compilationDto.id());
        assertEquals("Test", compilationDto.title());
        assertEquals(1, compilationDto.events().size());

        EventShortDto eventDto = compilationDto.events().getFirst();

        assertEquals(1L, eventDto.id());
        assertEquals(5L, eventDto.confirmedRequests());
        assertEquals(10L, eventDto.views());

        Mockito.verify(eventRepository).findAllById(List.of(1L));
        Mockito.verify(compilationRepository).save(Mockito.any(Compilation.class));
        Mockito.verify(participationRepository)
                .countByEventIdsAndStatus(List.of(1L), ParticipationStatus.CONFIRMED);
        Mockito.verify(eventViews).getView(List.of(1L));
    }

    @Test
    void createCompilationWithoutEventsTest() {
        NewCompilationDto newCompilationDto = createNewCompilationDto(null);

        Compilation compilation = createCompilation(List.of());

        when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenReturn(compilation);

        when(eventViews.getView(Mockito.anyList()))
                .thenReturn(Map.of());

        CompilationDto compilationDto = compilationService.createCompilation(newCompilationDto);

        assertEquals(1L, compilationDto.id());
        assertEquals("Test", compilationDto.title());
        assertTrue(compilationDto.events().isEmpty());

        Mockito.verify(eventRepository, Mockito.never()).findAllById(Mockito.anyList());
        Mockito.verify(compilationRepository).save(Mockito.any(Compilation.class));
        Mockito.verify(participationRepository, Mockito.never())
                .countByEventIdsAndStatus(Mockito.anyList(), Mockito.any());
        Mockito.verify(eventViews).getView(List.of());
    }

    @Test
    void deleteCompilationTest() {
        Compilation compilation = createCompilation(List.of(event));
        when(compilationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(compilation));

        compilationService.deleteCompilation(1L);
    }

    @Test
    void deleteCompilationNotExistCompilationShouldReturnNotFoundExceptionTest() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> compilationService.deleteCompilation(1L));

        assertEquals("Подборка событий id=1 не найдена", exception.getMessage());
    }

    @Test
    void updateCompilationAllFieldsTest() {
        Compilation compilation = createCompilation(List.of());
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .events(List.of(1L))
                .pinned(false)
                .title("Test")
                .build();

        when(compilationRepository.findByIdWithEvents(1L))
                .thenReturn(Optional.of(compilation));

        when(eventRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(event));

        when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenReturn(compilation);

        when(participationRepository.countByEventIdsAndStatus(
                Mockito.anyList(),
                Mockito.any(ParticipationStatus.class)))
                .thenReturn(List.of());

        when(eventViews.getView(Mockito.anyList()))
                .thenReturn(Map.of());

        CompilationDto result = compilationService.updateCompilation(1L, request);

        assertEquals("Test", result.title());
        assertEquals(false, result.pinned());
        assertEquals(1, result.events().size());

        Mockito.verify(eventRepository).findAllById(List.of(1L));
        Mockito.verify(compilationRepository).save(compilation);
    }

    @Test
    void updateCompilationTest() {
        Compilation compilation = createCompilation(List.of());
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .events(List.of(1L))
                .build();

        when(compilationRepository.findByIdWithEvents(1L))
                .thenReturn(Optional.of(compilation));

        when(eventRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(event));

        when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenReturn(compilation);

        when(participationRepository.countByEventIdsAndStatus(
                Mockito.anyList(),
                Mockito.any(ParticipationStatus.class)))
                .thenReturn(List.of());

        when(eventViews.getView(Mockito.anyList()))
                .thenReturn(Map.of());

        CompilationDto result = compilationService.updateCompilation(1L, request);

        assertEquals("Test", result.title());
        assertEquals(true, result.pinned());
        assertEquals(1, result.events().size());

        Mockito.verify(eventRepository).findAllById(List.of(1L));
        Mockito.verify(compilationRepository).save(compilation);
    }

    @Test
    void updateCompilationEventsNullTest() {
        Compilation compilation = createCompilation(List.of());

        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .build();

        when(compilationRepository.findByIdWithEvents(1L))
                .thenReturn(Optional.of(compilation));

        when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenReturn(compilation);

        when(eventViews.getView(Mockito.anyList()))
                .thenReturn(Map.of());

        CompilationDto result = compilationService.updateCompilation(1L, request);

        assertEquals("Test", result.title());
        assertTrue(result.pinned());
        assertEquals(0, result.events().size());

    }

    @Test
    void updateCompilationEventsEmptyTest() {
        Compilation compilation = createCompilation(List.of());

        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .events(List.of())
                .build();

        when(compilationRepository.findByIdWithEvents(1L))
                .thenReturn(Optional.of(compilation));

        when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenReturn(compilation);

        when(eventViews.getView(Mockito.anyList()))
                .thenReturn(Map.of());

        CompilationDto result = compilationService.updateCompilation(1L, request);

        assertEquals("Test", result.title());
        assertTrue(result.pinned());
        assertEquals(0, result.events().size());

    }

}
