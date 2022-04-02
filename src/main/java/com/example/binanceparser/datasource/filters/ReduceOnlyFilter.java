package com.example.binanceparser.datasource.filters;

import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.EventType;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;

public class ReduceOnlyFilter implements Filter {
    private boolean required;

    public ReduceOnlyFilter(boolean required) {
        this.required = required;
    }

    @Override
    public boolean filter(AbstractEvent event) {
        return event.getEventType() == EventType.FUTURES_ORDER_TRADE_UPDATE && ((FuturesOrderTradeUpdateEvent) event).isReduceOnly() == required;
    }
}
