package com.example.binanceparser.datasource.filters;

import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.EventType;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;

import java.util.List;

public class FuturesOrderStatusFilter implements Filter {
  private final List<OrderStatus> allowed;

  public FuturesOrderStatusFilter(List<OrderStatus> allowed) {
    this.allowed = allowed;
  }

  @Override
  public boolean filter(AbstractEvent event) {
    if (event.getEventType() != EventType.FUTURES_ORDER_TRADE_UPDATE)
      return false;
    FuturesOrderTradeUpdateEvent e = (FuturesOrderTradeUpdateEvent) event;
    return allowed.contains(e.getOrderStatus());
  }
}
