package com.example.binanceparser.domain;

import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ComplexEvent {
  private FuturesOrderTradeUpdateEvent filledEvent;
  private List<FuturesOrderTradeUpdateEvent> partiallyFilledEvents;

  public FuturesOrderTradeUpdateEvent getFirstValuableEvent() {
    return partiallyFilledEvents.isEmpty() ? filledEvent : partiallyFilledEvents.get(0);
  }
}
