package com.example.binanceparser;

import com.example.binanceparser.domain.events.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Config {
    LocalDateTime startTrackDate;
    LocalDateTime finishTrackDate;
    String inputFilepath;
    String outputDir;

}
