package com.metrotracker.service;

import com.metrotracker.client.PredictionClient;
import com.metrotracker.client.TrainPrediction;
import com.metrotracker.dto.TrainEstimateDto;
import com.metrotracker.client.PredictionResponse;
import io.netty.handler.timeout.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PredictionServiceTest {


    @Mock
    private PredictionClient predictionClient;
    private PredictionService predictionService;

    @BeforeEach
    void setUp() {
        predictionService = new PredictionService(predictionClient);
    }

    @Test
    void testGetTrainEstimates_withValidTrains() {
        TrainPrediction train = new TrainPrediction();
        train.setCar("8");
        train.setDestinationName("Downtown Largo");
        train.setLine("SV");
        train.setMin("6");

        PredictionResponse response = new PredictionResponse();
        response.setTrains(List.of(train));

        when(predictionClient.getPredictions("N06")).thenReturn(Mono.just(response));

        StepVerifier.create(predictionService.getTrainEstimates("N06"))
                .expectNextMatches(dto ->
                        dto.getCar().equals("8") &&
                                dto.getDestination().equals("Downtown Largo") &&
                                dto.getLine().equals("SV") &&
                                dto.getEtaMinutes() == 6
                )
                .verifyComplete();
    }

    @Test
    void testGetTrainEstimates_withInvalidMin_shouldFilterOut() {
        TrainPrediction train = new TrainPrediction();
        train.setMin("---");

        PredictionResponse response = new PredictionResponse();
        response.setTrains(List.of(train));

        when(predictionClient.getPredictions("N06")).thenReturn(Mono.just(response));

        StepVerifier.create(predictionService.getTrainEstimates("N06"))
                .expectComplete()
                .verify();
    }

    @Test
    void testGetTrainEstimates_withNullTrainList() {
        PredictionResponse response = new PredictionResponse();
        response.setTrains(null); // Simulate unexpected null

        when(predictionClient.getPredictions("N06")).thenReturn(Mono.just(response));

        StepVerifier.create(predictionService.getTrainEstimates("N06"))
                .expectComplete()
                .verify();
    }


    @Test
    void shouldFallbackToEmptyWhenTimeoutOccurs() {
        String stationCode = "N06";

        // Simulate timeout by returning a Mono that never completes
        Mockito.when(predictionClient.getPredictions(stationCode))
                .thenReturn(Mono.never());

        StepVerifier.create(predictionService.getTrainEstimates(stationCode))
                .expectSubscription()
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

}

