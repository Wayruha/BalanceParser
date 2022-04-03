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
        return event.getEventType() == EventType.FUTURES_ORDER_TRADE_UPDATE
                && allowed.stream().anyMatch((orderStatus) -> ((FuturesOrderTradeUpdateEvent) event).getOrderStatus().equals(orderStatus));
    }
}
