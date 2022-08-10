package com.example.binanceparser.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Config {
    private LocalDateTime startTrackDate;
    private LocalDateTime finishTrackDate;
    private String inputFilepath;
    private String outputDir;
    private String namesFilePath;
    private String reportOutputLocation;
    private String reportOutputDir;
    private List<String> subjects;
}
