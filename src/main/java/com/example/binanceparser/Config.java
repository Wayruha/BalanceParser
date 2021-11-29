package com.example.binanceparser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Config {
    LocalDateTime startTrackDate;
    LocalDateTime finishTrackDate;
    String inputFilepath;
    String outputDir;
    String logProducer;
}
