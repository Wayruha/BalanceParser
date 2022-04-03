package com.example.binanceparser.algorithm;

import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.domain.Stats;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.CLONE_POSTFIX;

public class FuturesStatsGenerationAlgorithm implements StatsGenerationAlgorithm<Stats> {
    private final StatsVisualizerConfig config;

    public FuturesStatsGenerationAlgorithm(StatsVisualizerConfig config) {
        this.config = config;
    }

    @Override
    public Stats getStats(List<AbstractEvent> events) {
        List<Stats.ComplexEvent> allEvents = groupToComplexEvents(filterRelevantEvents(events));
        Stats stats = new Stats(config);
        originalEvents(allEvents).forEach((originalEvent) -> {
            List<Stats.ComplexEvent> clonedEvents = clonedEvents(originalEvent, allEvents);
            if (!clonedEvents.isEmpty()) {
                stats.insertRecord(originalEvent, clonedEvents);
            }
        });
        return stats;
    }

    private List<FuturesOrderTradeUpdateEvent> filterRelevantEvents(List<AbstractEvent> events) {
        return events.stream().filter((event) -> config.getFilters().stream().allMatch((filter) -> filter.filter(event))).map((event) -> (FuturesOrderTradeUpdateEvent) event).collect(Collectors.toList());
    }

    private List<Stats.ComplexEvent> groupToComplexEvents(List<FuturesOrderTradeUpdateEvent> rawEvents) {
        Map<String, List<FuturesOrderTradeUpdateEvent>> mappedById = rawEvents.stream().collect(Collectors.groupingBy(FuturesOrderTradeUpdateEvent::getNewClientOrderId));
        return mappedById.keySet().stream().map((orderId) -> {
            Map<String, List<FuturesOrderTradeUpdateEvent>> mappedByUser = mappedById.get(orderId).stream().collect(Collectors.groupingBy(FuturesOrderTradeUpdateEvent::getSource));
            return mappedByUser.keySet().stream().map((user) -> {
                FuturesOrderTradeUpdateEvent filledEvent = mappedByUser.get(user).stream().filter((event) -> event.getOrderStatus() == OrderStatus.FILLED).findFirst().orElse(null);
                List<FuturesOrderTradeUpdateEvent> partiallyFilled = mappedByUser.get(user).stream().filter((event) -> event.getOrderStatus() == OrderStatus.PARTIALLY_FILLED).collect(Collectors.toList());
                return new Stats.ComplexEvent(filledEvent, partiallyFilled);
            }).collect(Collectors.toList());
        }).flatMap(Collection::stream).filter((event) -> event.getFilledEvent() != null).collect(Collectors.toList());
    }

    private List<Stats.ComplexEvent> clonedEvents(Stats.ComplexEvent originalEvent, List<Stats.ComplexEvent> allEvents) {
        return allEvents.stream().filter((event) -> event.getFilledEvent().getNewClientOrderId().equals(originalEvent.getFilledEvent().getNewClientOrderId() + CLONE_POSTFIX)).collect(Collectors.toList());
    }

    private List<Stats.ComplexEvent> originalEvents(List<Stats.ComplexEvent> allEvents) {
        return allEvents.stream().filter((event) -> !event.getFilledEvent().getNewClientOrderId().endsWith(CLONE_POSTFIX)).collect(Collectors.toList());
    }
}
