package com.example.binanceparser.datasource.filters;

import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.EventType;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;

public class FuturesOrderStatusFilter implements Filter {
    private final OrderStatus orderStatus;

    public FuturesOrderStatusFilter(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    @Override
    public boolean filter(AbstractEvent event) {
        return event.getEventType() == EventType.FUTURES_ORDER_TRADE_UPDATE && ((FuturesOrderTradeUpdateEvent) event).getOrderStatus().equals(orderStatus);
    }
}
