package com.metrotracker.controller;

import com.metrotracker.dto.TrainEstimateDto;
import com.metrotracker.service.PredictionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@WebFluxTest(controllers = PredictionController.class)
class PredictionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PredictionService predictionService;

    @Test
    void shouldReturnTrainEstimatesForValidStation() {
        String stationCode = "N06";

        TrainEstimateDto dto = new TrainEstimateDto(
                "8", "Downtown Largo", "SV", 6, 3900.0, stationCode, 38.947809, -77.340217
        );

        Mockito.when(predictionService.getTrainEstimates(stationCode))
                .thenReturn(Flux.just(dto));

        webTestClient.get()
                .uri("/api/stations/{stationCode}/predictions", stationCode)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TrainEstimateDto.class)
                .hasSize(1)
                .contains(dto);
    }

    @Test
    void shouldReturnEmptyListWhenNoTrains() {
        String stationCode = "N06";

        Mockito.when(predictionService.getTrainEstimates(stationCode))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/stations/{stationCode}/predictions", stationCode)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TrainEstimateDto.class)
                .hasSize(0);
    }

    @Test
    void shouldHandleServiceFailureGracefully() {
        String stationCode = "N06";

        Mockito.when(predictionService.getTrainEstimates(stationCode))
                .thenReturn(Flux.error(new RuntimeException("Service failure")));

        webTestClient.get()
                .uri("/api/stations/{stationCode}/predictions", stationCode)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}

