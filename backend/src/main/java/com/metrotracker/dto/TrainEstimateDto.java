package com.metrotracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrainEstimateDto {
    private String car;
    private String destination;
    private String line;
    private int etaMinutes;
    private double estimatedDistance;
    private String stationCode;
    private double lat;
    private double lon;
}


