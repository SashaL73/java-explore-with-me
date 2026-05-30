package ru.practicum.svc.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.svc.EndpointHit;
import ru.practicum.svc.ViewStats;
import ru.practicum.svc.exception.BadRequestException;
import ru.practicum.svc.model.Hit;
import ru.practicum.svc.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class HitServiceIntegrationTest {

    private final HitService hitService;
    private final HitRepository hitRepository;

    @Test
    void createHitTest() {
        EndpointHit endpointHit = EndpointHit.builder()
                .app("test")
                .ip("1.1.1.1")
                .uri("/test")
                .timestamp(LocalDateTime.now())
                .build();

        hitService.createHit(endpointHit);

        List<Hit> hits = hitRepository.findAll();

        assertEquals(1, hits.size());

    }

    @Test
    void getViewStatsTestUniqueFalse() {
        Hit hit1 = Hit.builder()
                .app("test")
                .ip("1.1.1.1")
                .uri("/test")
                .timestamp(LocalDateTime.now())
                .build();

        Hit hit2 = Hit.builder()
                .app("test")
                .ip("1.1.1.2")
                .uri("/test")
                .timestamp(LocalDateTime.now().plusSeconds(10))
                .build();

        hitRepository.save(hit1);
        hitRepository.save(hit2);

        List<ViewStats> viewStats = hitService.getViewStats(LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), false);

        assertEquals(1, viewStats.size());
        assertEquals(2, viewStats.getFirst().getHits());
    }

    @Test
    void getViewStatsTestUniqueTrue() {
        Hit hit1 = Hit.builder()
                .app("test")
                .ip("1.1.1.1")
                .uri("/test")
                .timestamp(LocalDateTime.now())
                .build();

        Hit hit2 = Hit.builder()
                .app("test")
                .ip("1.1.1.2")
                .uri("/test")
                .timestamp(LocalDateTime.now().plusSeconds(10))
                .build();

        Hit hit3 = Hit.builder()
                .app("test")
                .ip("1.1.1.2")
                .uri("/test")
                .timestamp(LocalDateTime.now().plusSeconds(10))
                .build();

        hitRepository.save(hit1);
        hitRepository.save(hit2);
        hitRepository.save(hit3);

        List<ViewStats> viewStats = hitService.getViewStats(LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), true);

        assertEquals(1, viewStats.size());
        assertEquals(2, viewStats.getFirst().getHits());
    }

    @Test
    void getViewStatsByUrisTestUniqueFalse() {
        Hit hit1 = Hit.builder()
                .app("test")
                .ip("1.1.1.1")
                .uri("/test")
                .timestamp(LocalDateTime.now())
                .build();

        Hit hit2 = Hit.builder()
                .app("test")
                .ip("1.1.1.2")
                .uri("/test/1")
                .timestamp(LocalDateTime.now().plusSeconds(10))
                .build();

        Hit hit3 = Hit.builder()
                .app("test")
                .ip("1.1.1.2")
                .uri("/test")
                .timestamp(LocalDateTime.now().plusSeconds(10))
                .build();

        hitRepository.save(hit1);
        hitRepository.save(hit2);
        hitRepository.save(hit3);

        String[] uris1 = {"/test", "/test/1"};
        String[] uris2 = {"/test"};

        List<ViewStats> viewStats1 = hitService.getViewStatsByUris(LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), uris1, false);

        assertEquals(2, viewStats1.size());
        assertEquals(2, viewStats1.getFirst().getHits());
        assertEquals(1, viewStats1.getLast().getHits());

        List<ViewStats> viewStats2 = hitService.getViewStatsByUris(LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), uris2, false);

        assertEquals(1, viewStats2.size());
        assertEquals(2, viewStats2.getFirst().getHits());

    }

    @Test
    void getViewStatsByUrisTestUniqueTrue() {
        Hit hit1 = Hit.builder()
                .app("test")
                .ip("1.1.1.2")
                .uri("/test")
                .timestamp(LocalDateTime.now())
                .build();


        Hit hit2 = Hit.builder()
                .app("test")
                .ip("1.1.1.2")
                .uri("/test")
                .timestamp(LocalDateTime.now().plusSeconds(10))
                .build();

        Hit hit3 = Hit.builder()
                .app("test")
                .ip("1.1.1.2")
                .uri("/test/1")
                .timestamp(LocalDateTime.now().plusSeconds(10))
                .build();

        Hit hit4 = Hit.builder()
                .app("test")
                .ip("1.1.1.2")
                .uri("/test/1")
                .timestamp(LocalDateTime.now().plusSeconds(10))
                .build();

        hitRepository.save(hit1);
        hitRepository.save(hit2);
        hitRepository.save(hit3);
        hitRepository.save(hit4);

        String[] uris1 = {"/test", "/test/1"};
        String[] uris2 = {"/test"};

        List<ViewStats> viewStats1 = hitService.getViewStatsByUris(LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), uris1, true);

        assertEquals(2, viewStats1.size());
        assertEquals(1, viewStats1.getFirst().getHits());
        assertEquals(1, viewStats1.getLast().getHits());

        List<ViewStats> viewStats2 = hitService.getViewStatsByUris(LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), uris2, true);

        assertEquals(1, viewStats2.size());
        assertEquals(1, viewStats2.getFirst().getHits());
    }

    @Test
    void getViewStatsTestShouldReturnBadRequestException() {

        assertThrows(BadRequestException.class, () -> hitService.getViewStats(LocalDateTime.now(),
                LocalDateTime.now().minusDays(1), false));

        assertThrows(BadRequestException.class, () -> hitService.getViewStatsByUris(LocalDateTime.now(),
                LocalDateTime.now().minusDays(1), null, false));
    }


}
