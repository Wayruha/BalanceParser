package com.example.binanceparser.algorithm;

import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.domain.Stats;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.CLONE_POSTFIX;

public class FuturesStatsGenerationAlgorithm implements StatsGenerationAlgorithm<Stats> {
    private final StatsVisualizerConfig config;

    public FuturesStatsGenerationAlgorithm(StatsVisualizerConfig config) {
        this.config = config;
    }

    @Override
    public Stats getStats(List<AbstractEvent> events) {
        List<FuturesOrderTradeUpdateEvent> allEvents = filterRelevantEvents(events);
        Stats stats = new Stats(config);
        originalEvents(allEvents).forEach((originalEvent) -> {
            List<FuturesOrderTradeUpdateEvent> clonedEvents = clonedEvents(originalEvent, allEvents);
            if (!clonedEvents.isEmpty()) {
                stats.insertRecord(originalEvent, clonedEvents);
            }
        });
        return stats;
    }

    private List<FuturesOrderTradeUpdateEvent> filterRelevantEvents(List<AbstractEvent> events) {
        return events.stream().filter((event) -> config.getFilters().stream().allMatch((filter) -> filter.filter(event))).map((event) -> (FuturesOrderTradeUpdateEvent) event).collect(Collectors.toList());
    }

    private List<FuturesOrderTradeUpdateEvent> clonedEvents(FuturesOrderTradeUpdateEvent originalEvent, List<FuturesOrderTradeUpdateEvent> allEvents) {
        return allEvents.stream().filter((event) -> event.getNewClientOrderId().equals(originalEvent.getNewClientOrderId() + CLONE_POSTFIX)).collect(Collectors.toList());
    }

    private List<FuturesOrderTradeUpdateEvent> originalEvents(List<FuturesOrderTradeUpdateEvent> allEvents) {
        return allEvents.stream().filter((event) -> !event.getNewClientOrderId().endsWith(CLONE_POSTFIX)).collect(Collectors.toList());
    }
}
