package com.metrotracker.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TrainPrediction {

      @JsonProperty("Car")
      private String car;

      @JsonProperty("Destination")
      private String destination;

      @JsonProperty("DestinationName")
      private String destinationName;

      @JsonProperty("Group")
      private String group;

      @JsonProperty("Line")
      private String line;

      @JsonProperty("LocationCode")
      private String locationCode;

      @JsonProperty("LocationName")
      private String locationName;

      @JsonProperty("Min")
      private String min;
}





