package com.example.binanceparser;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Config {
    LocalDateTime startTrackDate;
    LocalDateTime finishTrackDate;
    String sourceToTrack;
    String inputFilepath;
    String outputDir;
    List<String> assetsToTrack;
    boolean convertToUSD;
    String eventType; //TODO чого це стрінга?
}
