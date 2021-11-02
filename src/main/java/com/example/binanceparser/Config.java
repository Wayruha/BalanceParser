package com.example.binanceparser;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Config {
    LocalDateTime startTrackDate;
    LocalDateTime finishTrackDate;
    String sourceToTrack;
    String inputFilepath;
    String outputDir;
    String assetToTrack;
}
