package ru.practicum.svc.service;

import ru.practicum.svc.EndpointHit;
import ru.practicum.svc.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface HitService {
    void createHit(EndpointHit endpointHit);

    List<ViewStats> getViewStats(LocalDateTime start, LocalDateTime end, Boolean unique);

    List<ViewStats> getViewStatsByUris(LocalDateTime start, LocalDateTime end, String[] uri, Boolean unique);
}
