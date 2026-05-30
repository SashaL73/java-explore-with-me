package ru.practicum.svc.mapper;

import ru.practicum.svc.EndpointHit;
import ru.practicum.svc.ViewStats;
import ru.practicum.svc.model.Hit;
import ru.practicum.svc.repository.projection.ViewStatsProjection;

public class HitMapper {

    public static Hit mapToHit(EndpointHit endpointHit) {
        return Hit.builder()
                .ip(endpointHit.getIp())
                .uri(endpointHit.getUri())
                .app(endpointHit.getApp())
                .timestamp(endpointHit.getTimestamp())
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
