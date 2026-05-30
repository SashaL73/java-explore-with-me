package ru.practicum.svc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.svc.EndpointHit;
import ru.practicum.svc.ViewStats;
import ru.practicum.svc.exception.BadRequestException;
import ru.practicum.svc.mapper.HitMapper;
import ru.practicum.svc.model.Hit;
import ru.practicum.svc.repository.HitRepository;
import ru.practicum.svc.repository.projection.ViewStatsProjection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;

    @Override
    public void createHit(EndpointHit endpointHit) {
        Hit hit = HitMapper.mapToHit(endpointHit);
        hitRepository.save(hit);
        log.info("Сохранение hit id='{}',app='{}',uri='{}',ip='{}',timestamp='{}'",
                hit.getId(), hit.getApp(), hit.getUri(), hit.getIp(), hit.getTimestamp());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getViewStats(LocalDateTime start, LocalDateTime end, Boolean unique) {
        log.debug("Запрос на получение ViewStats start='{}', end='{}', unique='{}'",
                start, end, unique);
        chekLocalDataTime(start, end);
        List<ViewStatsProjection> hits;
        if (Boolean.TRUE.equals(unique)) {
            hits = hitRepository.searchHitsUniqueIp(start, end);
        } else {
            hits = hitRepository.searchHits(start, end);
        }

        return hits.stream()
                .map(HitMapper::mapViewStatsProjectionToViewStats)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getViewStatsByUris(LocalDateTime start, LocalDateTime end, String[] uri, Boolean unique) {
        log.debug("Запрос на получение ViewStats start='{}', end='{}', uri='{}', unique='{}'",
                start, end, uri, unique);
        chekLocalDataTime(start, end);
        List<ViewStatsProjection> hits;
        List<String> uris = new ArrayList<>(List.of(uri));
        if (Boolean.TRUE.equals(unique)) {
            hits = hitRepository.searchHitsByUrisAndUniqueIp(start, end, uris);
        } else {
            hits = hitRepository.searchHitsByUris(start, end, uris);
        }

        System.out.println("Получение статистики");

        return hits.stream()
                .map(HitMapper::mapViewStatsProjectionToViewStats)
                .toList();
    }

    private void chekLocalDataTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new BadRequestException("Дата конца раньше начала");
        }
    }

}
