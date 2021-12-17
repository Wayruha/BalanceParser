package com.example.binanceparser.config;

import com.example.binanceparser.domain.events.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
public class BalanceVisualizerConfig extends Config {
    private List<String> assetsToTrack;
    private boolean convertToUSD;
    private List<EventType> eventType;

    public BalanceVisualizerConfig() {
        assetsToTrack = Collections.emptyList();
    }
}
