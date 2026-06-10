package ru.practicum.ewm.event.service.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.svc.StatsClient;
import ru.practicum.svc.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventViews {

    private final StatsClient statsClient;

    public Map<Long, Long> getView(List<Long> eventsId) {
        if (eventsId == null || eventsId.isEmpty()) {
            return Map.of();
        }

        LocalDateTime now = LocalDateTime.now().plusSeconds(1);
        String uri = "/events/";
        List<String> uris = eventsId.stream()
                .map(id -> uri + id)
                .toList();

        List<ViewStats> viewStatsList = statsClient.getStats(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                now, uris, true);

        int index = uri.length();

        return viewStatsList.stream()
                .collect(Collectors.toMap(
                        viewStats -> Long.parseLong(viewStats.getUri().substring(index)),
                        ViewStats::getHits
                ));

    }
}
