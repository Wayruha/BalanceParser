package com.example.binanceparser.algorithm;

import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.domain.ComplexEvent;
import com.example.binanceparser.statistics.StatsDataHolder;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.binanceparser.Constants.CLONE_POSTFIX;

@Service
public class FuturesStatsGenerationAlgorithm implements StatsGenerationAlgorithm<StatsDataHolder> {
  private final StatsVisualizerConfig config;

  public FuturesStatsGenerationAlgorithm(StatsVisualizerConfig config) {
    this.config = config;
  }

  @Override
  public StatsDataHolder getStats(List<AbstractEvent> events) {
    List<ComplexEvent> allEvents = groupToComplexEvents(filterRelevantEvents(events));
    StatsDataHolder stats = new StatsDataHolder(config);
    findOnlyOriginalEvents(allEvents).forEach(originalEvent -> {
      List<ComplexEvent> clonedEvents = findAllClonedEvents(originalEvent, allEvents);
      if (!clonedEvents.isEmpty()) {
        stats.insertRecord(originalEvent, clonedEvents);
      }
    });
    return stats;
  }

  private List<FuturesOrderTradeUpdateEvent> filterRelevantEvents(List<AbstractEvent> events) {
    return events.stream()
        .filter(event -> config.getFilters().stream()
            .allMatch(filter -> filter.filter(event)))
        .map(event -> (FuturesOrderTradeUpdateEvent) event)
        .collect(Collectors.toList());
  }

  private List<ComplexEvent> groupToComplexEvents(List<FuturesOrderTradeUpdateEvent> rawEvents) {
    Map<String, List<FuturesOrderTradeUpdateEvent>> mappedById = rawEvents.stream()
        .collect(Collectors.groupingBy(FuturesOrderTradeUpdateEvent::getNewClientOrderId));
    return mappedById.keySet().stream().map(orderId -> {
      final Map<String, List<FuturesOrderTradeUpdateEvent>> mappedByUser = mappedById.get(orderId).stream()
          .collect(Collectors.groupingBy(FuturesOrderTradeUpdateEvent::getSource));
      return mappedByUser.keySet().stream().map(user -> {
        final FuturesOrderTradeUpdateEvent filledEvent = mappedByUser.get(user).stream()
            .filter(event -> event.getOrderStatus() == OrderStatus.FILLED)
            .findFirst().orElse(null);
        final List<FuturesOrderTradeUpdateEvent> partiallyFilled = mappedByUser.get(user).stream()
            .filter(event -> event.getOrderStatus() == OrderStatus.PARTIALLY_FILLED)
            .collect(Collectors.toList());
        return new ComplexEvent(filledEvent, partiallyFilled);
      }).collect(Collectors.toList());
    }).flatMap(Collection::stream).filter((event) -> event.getFilledEvent() != null).collect(Collectors.toList());
  }

  private List<ComplexEvent> findAllClonedEvents(ComplexEvent originalEvent, List<ComplexEvent> allEvents) {
    return allEvents.stream().filter(event -> isSameEventId(originalEvent, event)).collect(Collectors.toList());
  }

  private List<ComplexEvent> findOnlyOriginalEvents(List<ComplexEvent> allEvents) {
    return allEvents.stream().filter(event -> !isEventCloned(event)).collect(Collectors.toList());
  }

  private boolean isEventCloned(ComplexEvent event) {
    return event.getFilledEvent().getNewClientOrderId().endsWith(CLONE_POSTFIX);
  }

  private static boolean isSameEventId(ComplexEvent originalEvent, ComplexEvent event) {
    return event.getFilledEvent().getNewClientOrderId().equals(originalEvent.getFilledEvent().getNewClientOrderId() + CLONE_POSTFIX);
  }
}
