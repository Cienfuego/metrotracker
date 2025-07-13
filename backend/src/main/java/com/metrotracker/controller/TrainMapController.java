package com.metrotracker.controller;

import com.metrotracker.dto.TrainEstimateDto;
import com.metrotracker.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrainMapController {

    private final PredictionService predictionService;

    @GetMapping("/trains-near-station/{stationCode}")
    public Flux<TrainEstimateDto> getNearbyTrains(
            @PathVariable String stationCode,
            @RequestParam(defaultValue = "8000") double radiusMeters) {
        return predictionService.getNearbyTrains(stationCode, radiusMeters);
    }
}

