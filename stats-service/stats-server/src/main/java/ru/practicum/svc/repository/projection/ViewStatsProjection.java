package ru.practicum.svc.repository.projection;

public interface ViewStatsProjection {
    String getApp();

    String getUri();

    Long getHits();
}
