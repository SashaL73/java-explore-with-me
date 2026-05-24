package ru.practicum.svc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.svc.EndpointHit;
import ru.practicum.svc.service.HitService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HitController.class)
public class HitControllerTest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HitService hitService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createHitTest() throws Exception {
        EndpointHit endpointHit = EndpointHit.builder()
                .app("test")
                .ip("1.1.1.1")
                .uri("/test")
                .timestamp(LocalDateTime.now().format(formatter))
                .build();

        mockMvc.perform(post("/hit")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpointHit))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());


        Mockito.verify(hitService).createHit(Mockito.any(EndpointHit.class));
    }
}
