package ru.practicum.svc.mapper;

import ru.practicum.svc.EndpointHit;
import ru.practicum.svc.ViewStats;
import ru.practicum.svc.model.Hit;
import ru.practicum.svc.repository.projection.ViewStatsProjection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HitMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Hit mapToHit(EndpointHit endpointHit) {
        return Hit.builder()
                .ip(endpointHit.getIp())
                .uri(endpointHit.getUri())
                .app(endpointHit.getApp())
                .timestamp(LocalDateTime.parse(endpointHit.getTimestamp(), formatter))
                .build();
    }

    public static ViewStats mapViewStatsProjectionToViewStats(ViewStatsProjection viewStatsProjection) {
        return ViewStats.builder()
                .app(viewStatsProjection.getApp())
                .uri(viewStatsProjection.getUri())
                .hits(viewStatsProjection.getHits())
                .build();
    }
}
