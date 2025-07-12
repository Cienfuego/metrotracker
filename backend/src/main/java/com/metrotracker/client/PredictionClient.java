package com.metrotracker.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PredictionClient {

    private final WebClient webClient;

    public PredictionClient(@Value("${wmata.base-url}") String baseUrl,
                            @Value("${wmata.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("api_key", apiKey)
                .build();
    }

    public Mono<PredictionResponse> getPredictions(String stationCode) {
        return webClient.get()
                .uri("/StationPrediction.svc/json/GetPrediction/{stationCode}", stationCode)
                .retrieve()
                .bodyToMono(PredictionResponse.class)
                .doOnNext(json -> System.out.println("RAW WMATA RESPONSE:\n" + json));
    }
}

