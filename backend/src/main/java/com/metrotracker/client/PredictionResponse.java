package com.metrotracker.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictionResponse {
    @JsonProperty("Trains")
    private List<TrainPrediction> Trains;
}

