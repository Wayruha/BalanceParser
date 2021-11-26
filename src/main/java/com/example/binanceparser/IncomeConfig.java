package com.example.binanceparser;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class IncomeConfig extends Config {
    public IncomeConfig(LocalDateTime startTrackDate, LocalDateTime finishTrackDate, String inputFilepath, String outputDir) {
        super(startTrackDate, finishTrackDate, inputFilepath, outputDir);
    }
}
