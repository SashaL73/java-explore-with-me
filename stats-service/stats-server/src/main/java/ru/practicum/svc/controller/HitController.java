package ru.practicum.svc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.svc.EndpointHit;
import ru.practicum.svc.service.HitService;

@RestController
@RequiredArgsConstructor
public class HitController {

    private final HitService hitService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createHit(@RequestBody EndpointHit endpointHit) {
        hitService.createHit(endpointHit);
    }
}