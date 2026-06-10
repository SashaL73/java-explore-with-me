package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.service.view.EventViews;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class CompilationServiceIntegrationTest {

    private final CompilationService compilationService;
    private final CompilationRepository compilationRepository;

    @MockBean
    private EventViews eventViews;

    @Test
    void getCompilationsTest() {
        when(eventViews.getView(Mockito.anyList()))
                .thenReturn(Map.of());

        Compilation compilation1 = Compilation.builder()
                .title("Test1")
                .pinned(true)
                .events(List.of())
                .build();

        Compilation compilation2 = Compilation.builder()
                .title("Test2")
                .pinned(false)
                .events(List.of())
                .build();

        compilationRepository.save(compilation1);
        compilationRepository.save(compilation2);

        List<CompilationDto> result = compilationService.getCompilations(true, 0L, 10L);

        assertEquals(1, result.size());
        assertEquals("Test1", result.getFirst().title());
        assertTrue(result.getFirst().pinned());
    }

        @Test
        void getCompilationsPinnedNullTest() {
            when(eventViews.getView(Mockito.anyList()))
                    .thenReturn(Map.of());

            Compilation compilation1 = Compilation.builder()
                    .title("Test1")
                    .pinned(true)
                    .events(List.of())
                    .build();

            Compilation compilation2 = Compilation.builder()
                    .title("Test2")
                    .pinned(false)
                    .events(List.of())
                    .build();

            compilationRepository.save(compilation1);
            compilationRepository.save(compilation2);

            List<CompilationDto> result = compilationService.getCompilations(null, 0L, 10L);

            assertEquals(2, result.size());
            assertEquals("Test1", result.getFirst().title());
            assertEquals("Test2", result.getLast().title());
            assertTrue(result.getFirst().pinned());
            assertFalse(result.getLast().pinned());
    }

}
