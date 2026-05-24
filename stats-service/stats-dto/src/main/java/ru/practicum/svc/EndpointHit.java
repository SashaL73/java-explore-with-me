package ru.practicum.svc;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHit {
    private String app;
    private String uri;
    private String ip;
    private String timestamp;
}
