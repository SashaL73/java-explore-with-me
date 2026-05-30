package ru.practicum.svc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class StatsClient {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final String statsServerUrl;


    public void saveHit(EndpointHit endpointHitDto) {
        restTemplate.postForEntity(statsServerUrl + "/hit", endpointHitDto, Void.class);
    }

    public List<ViewStats> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    Boolean unique) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(statsServerUrl + "/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);
        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris);
        }

        URI uri = builder.build().encode().toUri();

        ResponseEntity<ViewStats[]> responseEntity = restTemplate.getForEntity(uri, ViewStats[].class);

        ViewStats[] viewStats = responseEntity.getBody();

        if (viewStats == null) {
            return List.of();
        } else {
            return List.of(viewStats);
        }
    }
}
