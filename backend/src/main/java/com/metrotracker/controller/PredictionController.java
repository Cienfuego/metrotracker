package com.metrotracker.controller;

import com.metrotracker.dto.TrainEstimateDto;
import com.metrotracker.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @GetMapping("/{stationCode}/predictions")
    public Flux<TrainEstimateDto> getPredictions(@PathVariable String stationCode) {
        return predictionService.getTrainEstimates(stationCode);
    }
}

