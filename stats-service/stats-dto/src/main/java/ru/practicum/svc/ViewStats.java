package ru.practicum.svc;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ViewStats {

    private String app;

    private String uri;

    private Long hits;

}
