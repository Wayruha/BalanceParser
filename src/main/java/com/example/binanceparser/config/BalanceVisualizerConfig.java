package com.example.binanceparser.config;

import com.example.binanceparser.domain.events.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceVisualizerConfig extends Config {
    List<String> assetsToTrack;
    boolean convertToUSD;
    List<EventType> eventType;
}
