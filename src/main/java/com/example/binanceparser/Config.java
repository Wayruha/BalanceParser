package com.example.binanceparser;

import com.example.binanceparser.domain.events.EventType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Config {
    LocalDateTime startTrackDate;
    LocalDateTime finishTrackDate;
    List<String> sourceToTrack;
    String inputFilepath;
    String outputDir;
    List<String> assetsToTrack;
    boolean convertToUSD;
    List<EventType> eventType;
}
