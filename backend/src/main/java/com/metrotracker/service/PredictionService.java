package com.metrotracker.service;

import com.metrotracker.client.PredictionClient;
import com.metrotracker.client.TrainPrediction;
import com.metrotracker.dto.TrainEstimateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionClient predictionClient;

    private static final double METERS_PER_MINUTE = 650;

    // Hardcoded coordinates for demo purposes
    private static final java.util.Map<String, double[]> STATION_COORDINATES = java.util.Map.of(
            "N06", new double[]{38.947809, -77.340217},      // Wiehle-Reston East
            "N07", new double[]{38.949685, -77.365398}       // Reston Town Center
    );

    public Flux<TrainEstimateDto> getTrainEstimates(String stationCode) {
        double[] coords = STATION_COORDINATES.getOrDefault(stationCode, new double[]{0, 0});

        return predictionClient.getPredictions(stationCode)
                .flatMapMany(resp -> {
                    var trains = resp.getTrains();
                    return Flux.fromIterable(trains != null ? trains : List.of());
                })
                .flatMap(train -> {
                    String minRaw = train.getMin();
                    int etaMin;

                    if (minRaw == null || minRaw.isBlank()) {
                        return Mono.empty(); // skip blank ETA values
                    }

                    if ("BRD".equalsIgnoreCase(minRaw) || "ARR".equalsIgnoreCase(minRaw)) {
                        etaMin = 0;
                    } else {
                        try {
                            etaMin = Integer.parseInt(minRaw);
                        } catch (NumberFormatException e) {
                            return Mono.empty(); // skip malformed ETA values
                        }
                    }

                    double estimatedDistance = etaMin * METERS_PER_MINUTE;

                    return Mono.just(new TrainEstimateDto(
                            train.getCar(),
                            train.getDestinationName(),
                            train.getLine(),
                            etaMin,
                            estimatedDistance,
                            stationCode,
                            coords[0],
                            coords[1]
                    ));
                });
    }
}
