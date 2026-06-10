package ru.practicum.ewm.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicCompilationController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PublicCompilationControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    private final EventShortDto eventShortDto = EventShortDto.builder()
            .annotation("TestAnnotation")
            .id(1L)
            .title("Test")
            .category(CategoryDto.builder()
                    .name("TestCategory")
                    .id(1L)
                    .build())
            .confirmedRequests(1L)
            .eventDate(LocalDateTime.of(2026, 6, 6, 6, 1))
            .initiator(UserShortDto.builder()
                    .id(1L)
                    .name("TestName")
                    .build())
            .views(0L)
            .paid(true)
            .build();

    private final CompilationDto compilationDto = CompilationDto.builder()
            .events(List.of(eventShortDto))
            .id(1L)
            .pinned(true)
            .title("TestTitle")
            .build();

    @Test
    void getCompilationsTest() throws Exception {
        List<CompilationDto> compilationDtos = List.of(compilationDto);

        when(compilationService.getCompilations(Mockito.anyBoolean(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(compilationDtos);

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].title").value("TestTitle"))
                .andExpect(jsonPath("$.[0].events", hasSize(1)))
                .andExpect(jsonPath("$.[0].events.[0].id").value(1))
                .andExpect(jsonPath("$.[0].events.[0].category.id").value(1))
                .andExpect(jsonPath("$.[0].events.[0].initiator.id").value(1));
    }

    @Test
    void getCompilationTest() throws Exception {
        when(compilationService.getCompilation(Mockito.anyLong()))
                .thenReturn(compilationDto);

        mockMvc.perform(get("/compilations/{compId}", 1)
                        .param("pinned", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("TestTitle"))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events.[0].id").value(1))
                .andExpect(jsonPath("$.events.[0].category.id").value(1))
                .andExpect(jsonPath("$.events.[0].initiator.id").value(1));
    }

    @Test
    void getCompilationIncorrectPathTest() throws Exception {
        when(compilationService.getCompilation(Mockito.anyLong()))
                .thenReturn(compilationDto);

        mockMvc.perform(get("/compilations/{compId}", 0)
                        .param("pinned", "true"))
                .andExpect(status().isBadRequest());
    }
}
