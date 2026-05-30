package ru.practicum.ewm.compilation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AdminCompilationController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AdminCompilationControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;

    private NewCompilationDto getNewCompilationDto(List<Long> events, Boolean pinned, String title) {
        return NewCompilationDto.builder()
                .events(events)
                .pinned(pinned)
                .title(title)
                .build();
    }

    private UpdateCompilationRequest getUpdateCompilationRequest(List<Long> events, Boolean pinned, String title) {
        return UpdateCompilationRequest.builder()
                .events(events)
                .pinned(pinned)
                .title(title)
                .build();
    }

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
    void createCompilationTest() throws Exception {
        NewCompilationDto newCompilationDto = getNewCompilationDto(
                List.of(1L), true, "Test"
        );

        when(compilationService.createCompilation(Mockito.any(NewCompilationDto.class)))
                .thenReturn(compilationDto);

        mockMvc.perform(post("/admin/compilations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pinned").value(true))
                .andExpect(jsonPath("$.title").value("TestTitle"))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events.[0].id").value(1))
                .andExpect(jsonPath("$.events.[0].category.id").value(1))
                .andExpect(jsonPath("$.events.[0].initiator.id").value(1));

    }

    @Test
    void createCompilationIncorrectListEventsShouldReturnBadRequestTest() throws Exception {
        NewCompilationDto newCompilationDto = getNewCompilationDto(
                List.of(0L), true, "Test"
        );

        mockMvc.perform(post("/admin/compilations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCompilationIncorrectTitleEventsShouldReturnBadRequestTest() throws Exception {
        NewCompilationDto newCompilationDto = getNewCompilationDto(
                List.of(1L), true, "t".repeat(51)
        );

        mockMvc.perform(post("/admin/compilations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        NewCompilationDto newCompilationDto1 = getNewCompilationDto(
                List.of(1L), true, null
        );

        mockMvc.perform(post("/admin/compilations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto1))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCompilationTest() throws Exception {
        mockMvc.perform(delete("/admin/compilations/{compId}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCompilationIncorrectIdCompilationShouldReturnBadRequestTest() throws Exception {
        mockMvc.perform(delete("/admin/compilations/{compId}", 0))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCompilationTest() throws Exception {
        UpdateCompilationRequest updateRequest = getUpdateCompilationRequest(
                List.of(1L), true, "Test"
        );

        when(compilationService.updateCompilation(Mockito.anyLong(), Mockito.any(UpdateCompilationRequest.class)))
                .thenReturn(compilationDto);

        mockMvc.perform(patch("/admin/compilations/{compId}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pinned").value(true))
                .andExpect(jsonPath("$.title").value("TestTitle"))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events.[0].id").value(1))
                .andExpect(jsonPath("$.events.[0].category.id").value(1))
                .andExpect(jsonPath("$.events.[0].initiator.id").value(1));
    }

    @Test
    void updateCompilationIncorrectPathIdShouldReturnBadRequestTest() throws Exception {
        UpdateCompilationRequest updateRequest = getUpdateCompilationRequest(
                List.of(1L), true, "Test"
        );

        mockMvc.perform(patch("/admin/compilations/{compId}", 0)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCompilationIncorrectBodyShouldReturnBadRequestTest() throws Exception {
        UpdateCompilationRequest updateRequest = getUpdateCompilationRequest(
                List.of(0L), true, "Test"
        );

        mockMvc.perform(patch("/admin/compilations/{compId}", 0)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        UpdateCompilationRequest updateRequest1 = getUpdateCompilationRequest(
                List.of(1L), true, "t".repeat(51)
        );

        mockMvc.perform(patch("/admin/compilations/{compId}", 0)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest1))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }
}
