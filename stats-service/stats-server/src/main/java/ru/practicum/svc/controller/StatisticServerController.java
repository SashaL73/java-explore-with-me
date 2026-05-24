package ru.practicum.svc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.svc.ViewStats;
import ru.practicum.svc.service.HitService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatisticServerController {

    private final HitService hitService;


    @GetMapping
    public List<ViewStats> getViewStats(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) String[] uris,
            @RequestParam(required = false) Boolean unique) {
        if (uris == null || uris.length == 0) {
            return hitService.getViewStats(start, end, unique);
        } else {
            return hitService.getViewStatsByUris(start, end, uris, unique);
        }
    }

}
