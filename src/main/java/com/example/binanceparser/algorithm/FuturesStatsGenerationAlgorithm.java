package com.example.binanceparser.algorithm;

import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.domain.FuturesStats;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;

import java.util.ArrayList;
import java.util.List;

public class FuturesStatsGenerationAlgorithm implements StatsGenerationAlgorithm<FuturesStats> {
    private StatsVisualizerConfig config;

    public FuturesStatsGenerationAlgorithm(StatsVisualizerConfig config) {
        this.config = config;
    }

    @Override
    public List<FuturesStats> getStats(List<AbstractEvent> events) {
        List<FuturesOrderTradeUpdateEvent> relevantEvents = filterRelevantEvents(events);
        List<FuturesStats> stats = new ArrayList<>();
        return stats;
    }

    private List<FuturesOrderTradeUpdateEvent> filterRelevantEvents(List<AbstractEvent> events) {
        return null;
    }
}
