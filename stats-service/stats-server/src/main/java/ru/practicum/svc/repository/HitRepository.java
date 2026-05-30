package ru.practicum.svc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.svc.model.Hit;
import ru.practicum.svc.repository.projection.ViewStatsProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query("select h.app as app, " +
            "h.uri as uri, " +
            "count(h.id) as hits " +
            "from Hit h " +
            "where h.timestamp >= :start " +
            "and h.timestamp <= :end " +
            "and h.uri in :uris " +
            "group by h.app, h.uri " +
            "order by count(h.id) desc")
    List<ViewStatsProjection> searchHitsByUris(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("uris") List<String> uris);

    @Query("select h.app as app, " +
            "h.uri as uri, " +
            "count(distinct h.ip) as hits " +
            "from Hit h " +
            "where h.timestamp >= :start " +
            "and h.timestamp <= :end " +
            "and h.uri in :uris " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<ViewStatsProjection> searchHitsByUrisAndUniqueIp(@Param("start") LocalDateTime start,
                                                          @Param("end") LocalDateTime end,
                                                          @Param("uris") List<String> uris);

    @Query("select h.app as app, " +
            "h.uri as uri, " +
            "count(h.id) as hits " +
            "from Hit h " +
            "where h.timestamp >= :start " +
            "and h.timestamp <= :end " +
            "group by h.app, h.uri " +
            "order by count(h.id) desc")
    List<ViewStatsProjection> searchHits(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("select h.app as app, " +
            "h.uri as uri, " +
            "count(distinct h.ip) as hits " +
            "from Hit h " +
            "where h.timestamp >= :start " +
            "and h.timestamp <= :end " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<ViewStatsProjection> searchHitsUniqueIp(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);


}
