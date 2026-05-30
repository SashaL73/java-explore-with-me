package ru.practicum.ewm.participation.repository.projection;

public interface RequestsCountProjection {

    Long getEventId();

    Long getCountRequests();
}
