package com.metrotracker.service;

import com.metrotracker.client.PredictionClient;
import com.metrotracker.client.PredictionResponse;
import com.metrotracker.dto.TrainEstimateDto;
import com.metrotracker.client.TrainPrediction;
import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionClient predictionClient;

    private static final double METERS_PER_MINUTE = 650;

    // Approximate coordinates for supported stations
    private static final Map<String, double[]> STATION_COORDINATES = new HashMap<>();

    static {
        STATION_COORDINATES.put("N06", new double[]{38.947809, -77.340217}); // Wiehle-Reston East
        STATION_COORDINATES.put("N07", new double[]{38.951776, -77.362146}); // Reston Town Center (optional)
    }

    public Flux<TrainEstimateDto> getTrainEstimates(String stationCode) {
        double[] coords = STATION_COORDINATES.getOrDefault(stationCode, new double[]{0, 0});

//        return predictionClient.getPredictions(stationCode)
//                .flatMapMany(resp -> {
//                    List<TrainPrediction> trains = resp.getTrains();
//                    return Flux.fromIterable(trains != null ? trains : List.of());
//                })
//                .filter(train -> train.getMin() != null && !"---".equals(train.getMin()) && !"".equals(train.getMin()))
//                .map(train -> {
//                    int etaMin;
//                    try {
//                        etaMin = ("BRD".equalsIgnoreCase(train.getMin()) || "ARR".equalsIgnoreCase(train.getMin()))
//                                ? 0 : Integer.parseInt(train.getMin());
//                    } catch (NumberFormatException e) {
//                        return null; // skip train with invalid ETA
//                    }
//
//                    double estimatedDistance = etaMin * METERS_PER_MINUTE;
//
//                    double[] offsetCoords = applyOffset(coords[0], coords[1], estimatedDistance, train.getDestinationName());
//
//                    return new TrainEstimateDto(
//                            train.getCar(),
//                            train.getDestinationName(),
//                            train.getLine(),
//                            etaMin,
//                            estimatedDistance,
//                            stationCode,
//                            offsetCoords[0],
//                            offsetCoords[1]
//                    );
//                })
//                .filter(dto -> dto != null);
//    }
        return predictionClient.getPredictions(stationCode)
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(TimeoutException.class, e -> {
                    System.err.println("Timeout fetching predictions for station: " + stationCode);
                    return Mono.just(new PredictionResponse());
                })
                .onErrorResume(e -> {
                    System.err.println("Error fetching predictions: " + e.getMessage());
                    return Mono.just(new PredictionResponse());
                })
                .flatMapMany(resp -> {
                    List<TrainPrediction> trains = resp.getTrains();
                    return Flux.fromIterable(trains != null ? trains : List.of());
                })
                .filter(train -> train.getMin() != null && !"---".equals(train.getMin()) && !"".equals(train.getMin()))
                .map(train -> {
                    int etaMin;
                    try {
                        etaMin = "BRD".equalsIgnoreCase(train.getMin()) || "ARR".equalsIgnoreCase(train.getMin())
                                ? 0 : Integer.parseInt(train.getMin());
                    } catch (NumberFormatException e) {
                        etaMin = -1;
                    }

                    if (etaMin < 0) return null;

                    double estimatedDistance = etaMin * METERS_PER_MINUTE;
                    double[] offsetCoords = applyOffset(coords[0], coords[1], estimatedDistance, train.getDestinationName());

                    return new TrainEstimateDto(
                            train.getCar(),
                            train.getDestinationName(),
                            train.getLine(),
                            etaMin,
                            estimatedDistance,
                            stationCode,
                            offsetCoords[0],
                            offsetCoords[1]
                    );
                })
                .filter(dto -> dto != null);
    }

    public Flux<TrainEstimateDto> getNearbyTrains(String stationCode, double maxDistanceMeters) {
        return getTrainEstimates(stationCode)
                .filter(train -> train.getEstimatedDistance() <= maxDistanceMeters);
    }

    /**
     * Offset coordinates east or west depending on train destination.
     */
    private double[] applyOffset(double lat, double lon, double distanceMeters, String destinationName) {
        // Approximate degrees per meter at Reston
        double lonMeters = 85000.0;

        boolean headingEast = destinationName != null &&
                (destinationName.toLowerCase().contains("newcrlton") ||
                        destinationName.toLowerCase().contains("downtown largo") ||
                        destinationName.toLowerCase().contains("largo"));

        double offsetLon = lon + (!headingEast ? 1 : -1) * (distanceMeters / lonMeters);

        return new double[]{lat, offsetLon};
    }
}


//package com.metrotracker.service;
//
//import com.metrotracker.client.PredictionClient;
//import com.metrotracker.dto.TrainEstimateDto;
//import com.metrotracker.client.TrainPrediction;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class PredictionService {
//
//    private final PredictionClient predictionClient;
//
//    private static final double METERS_PER_MINUTE = 650;
//
//    // Approximate coordinates for supported stations
//    private static final Map<String, double[]> STATION_COORDINATES = new HashMap<>();
//
//    static {
//        STATION_COORDINATES.put("N06", new double[]{38.947809, -77.340217}); // Wiehle-Reston East
//        STATION_COORDINATES.put("N07", new double[]{38.951776, -77.362146}); // Reston Town Center (optional later)
//    }
//
//    public Flux<TrainEstimateDto> getTrainEstimates(String stationCode) {
//        double[] coords = STATION_COORDINATES.getOrDefault(stationCode, new double[]{0, 0});
//
//        return predictionClient.getPredictions(stationCode)
//                .flatMapMany(resp -> {
//                    List<TrainPrediction> trains = resp.getTrains();
//                    return Flux.fromIterable(trains != null ? trains : List.of());
//                })
//                .filter(train -> train.getMin() != null && !"---".equals(train.getMin()) && !"".equals(train.getMin()))
//                .map(train -> {
//                    int etaMin;
//                    try {
//                        etaMin = "BRD".equalsIgnoreCase(train.getMin()) || "ARR".equalsIgnoreCase(train.getMin())
//                                ? 0 : Integer.parseInt(train.getMin());
//                    } catch (NumberFormatException e) {
//                        etaMin = -1; // Skip invalid/min values
//                    }
//
//                    if (etaMin < 0) return null;
//
//                    double estimatedDistance = etaMin * METERS_PER_MINUTE;
//                    double[] offsetCoords = applyOffset(coords[0], coords[1], estimatedDistance, train.getDestinationName());
//                    return new TrainEstimateDto(
//                            train.getCar(),
//                            train.getDestinationName(),
//                            train.getLine(),
//                            etaMin,
//                            estimatedDistance,
//                            stationCode,
////                            coords[0],
////                            coords[1]
//                            offsetCoords[0],
//                            offsetCoords[1]
//                    );
//                })
//                .filter(dto -> dto != null);
//    }
//
//    public Flux<TrainEstimateDto> getNearbyTrains(String stationCode, double maxDistanceMeters) {
//        return getTrainEstimates(stationCode)
//                .filter(train -> train.getEstimatedDistance() <= maxDistanceMeters);
//    }
//
//    private double[] applyOffset(double lat, double lon, double distanceMeters, String destinationName) {
//        // Approximate conversion: 1 deg latitude ≈ 111,000m; 1 deg longitude ≈ 85,000m at Reston
//        double latMeters = 111000.0;
//        double lonMeters = 85000.0;
//
////        boolean headingEast = destinationName != null &&
////                (destinationName.contains("Largo") || destinationName.contains("NewCrlton"));
////
////        double bearing = headingEast ? 1 : -1;
////
////        double offsetLat = lat;
////        double offsetLon = lon + bearing * (distanceMeters / lonMeters);
////
////        return new double[]{offsetLat, offsetLon};
//        boolean headingEast = destinationName != null &&
//                (destinationName.toLowerCase().contains("newcrlton") ||
//                        destinationName.toLowerCase().contains("downtown largo") ||
//                        destinationName.toLowerCase().contains("largo"));
//
//        double offsetLon = lon + (headingEast ? 1 : -1) * (distanceMeters / lonMeters);
//
//        return new double[]{lat, offsetLon};
//    }
//}
//
