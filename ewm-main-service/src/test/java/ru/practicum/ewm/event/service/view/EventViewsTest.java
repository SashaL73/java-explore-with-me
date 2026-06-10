package ru.practicum.ewm.event.service.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.svc.StatsClient;
import ru.practicum.svc.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventViewsTest {

    @Mock
    private StatsClient statsClient;

    @InjectMocks
    private EventViews eventViews;

    @Test
    void getViewEventsIdIsNull() {
        Map<Long, Long> result = eventViews.getView(null);
        assertTrue(result.isEmpty());
        verifyNoInteractions(statsClient);
    }

    @Test
    void getViewEventsIdIsEmpty() {
        Map<Long, Long> result = eventViews.getView(List.of());
        assertTrue(result.isEmpty());
        verifyNoInteractions(statsClient);
    }

    @Test
    void getViewExistStat() {
        List<Long> eventIds = List.of(1L, 2L);

        List<ViewStats> stats = List.of(
                new ViewStats("ewm-main-service", "/events/1", 10L),
                new ViewStats("ewm-main-service", "/events/2", 5L)
        );

        when(statsClient.getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList(),
                anyBoolean()
        )).thenReturn(stats);

        Map<Long, Long> result = eventViews.getView(eventIds);

        assertEquals(2, result.size());
        assertEquals(10L, result.get(1L));
        assertEquals(5L, result.get(2L));
    }
}