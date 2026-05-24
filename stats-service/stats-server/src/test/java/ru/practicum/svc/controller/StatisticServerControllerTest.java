package ru.practicum.svc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.svc.ViewStats;
import ru.practicum.svc.exception.BadRequestException;
import ru.practicum.svc.service.HitService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class StatisticServerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    HitService hitService;

    @Autowired
    ObjectMapper objectMapper;


    @Test
    void getViewStatsTest() throws Exception {
        ViewStats viewStats1 = ViewStats.builder()
                .app("test")
                .uri("/test")
                .hits(1L)
                .build();

        ViewStats viewStats2 = ViewStats.builder()
                .app("test")
                .uri("/test/1")
                .hits(1L)
                .build();

        List<ViewStats> viewStats = List.of(viewStats1, viewStats2);

        Mockito.when(hitService.getViewStatsByUris(Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class), Mockito.any(String[].class), Mockito.anyBoolean()))
                .thenReturn(viewStats);

        mockMvc.perform(get("/stats")
                        .param("start", "2026-05-22 00:00:00")
                        .param("end", "2026-05-23 00:00:00")
                        .param("unique", "true")
                        .param("uris", "/test", "/test/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("test"))
                .andExpect(jsonPath("$[0].uri").value("/test"))
                .andExpect(jsonPath("$[0].hits").value(1))
                .andExpect(jsonPath("$[1].app").value("test"))
                .andExpect(jsonPath("$[1].uri").value("/test/1"))
                .andExpect(jsonPath("$[1].hits").value(1));

        Mockito.when(hitService.getViewStats(Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class), Mockito.anyBoolean()))
                .thenReturn(viewStats);

        mockMvc.perform(get("/stats")
                        .param("start", "2026-05-22 00:00:00")
                        .param("end", "2026-05-23 00:00:00")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("test"))
                .andExpect(jsonPath("$[0].uri").value("/test"))
                .andExpect(jsonPath("$[0].hits").value(1))
                .andExpect(jsonPath("$[1].app").value("test"))
                .andExpect(jsonPath("$[1].uri").value("/test/1"))
                .andExpect(jsonPath("$[1].hits").value(1));

        mockMvc.perform(get("/stats")
                        .param("start", "2026-05-22 00:00:00")
                        .param("end", "2026-05-23 00:00:00")
                        .param("unique", "true")
                        .param("uris", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("test"))
                .andExpect(jsonPath("$[0].uri").value("/test"))
                .andExpect(jsonPath("$[0].hits").value(1))
                .andExpect(jsonPath("$[1].app").value("test"))
                .andExpect(jsonPath("$[1].uri").value("/test/1"))
                .andExpect(jsonPath("$[1].hits").value(1));

    }

    @Test
    void getViewStatsTestShouldResponseBadRequestException() throws Exception {

        Mockito.when(hitService.getViewStats(Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class), Mockito.anyBoolean()
        )).thenThrow(new BadRequestException("Дата конца раньше начала"));

        mockMvc.perform(get("/stats")
                        .param("start", "2026-05-22 00:00:00")
                        .param("end", "2024-05-23 00:00:00")
                        .param("unique", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertInstanceOf(BadRequestException.class, exception);
                    assertEquals("Дата конца раньше начала", exception.getMessage());
                });

    }

}
