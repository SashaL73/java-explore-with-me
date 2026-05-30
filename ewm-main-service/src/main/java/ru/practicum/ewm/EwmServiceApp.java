package ru.practicum.ewm;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.practicum.svc.EndpointHit;
import ru.practicum.svc.StatsClient;
import ru.practicum.svc.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootApplication
public class EwmServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EwmServiceApp.class);
    }


    @Bean
    public CommandLineRunner commandLineRunner(StatsClient statsClient) {
        return args -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            EndpointHit endpointHit = EndpointHit.builder()
                    .app("ewm-main-service")
                    .ip("1.1.1.1")
                    .uri("/events")
                    .timestamp(LocalDateTime.parse(LocalDateTime.now().format(formatter)))
                    .build();

            statsClient.saveHit(endpointHit);


            List<ViewStats> viewStats = statsClient.getStats(LocalDateTime.now().minusDays(1),
                    LocalDateTime.now(), null, false);

            viewStats.forEach(System.out::println);
        };
    }
}
