/*
package com.example.binanceparser.domain;

import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static com.example.binanceparser.Constants.*;

public class FuturesStats extends Stats {
    private FuturesOrderTradeUpdateEvent originalEvent;
    private List<FuturesOrderTradeUpdateEvent> clonedEvents;

    public FuturesStats(FuturesOrderTradeUpdateEvent originalEvent, List<FuturesOrderTradeUpdateEvent> clonedEvents) {
        this.originalEvent = originalEvent;
        this.clonedEvents = clonedEvents;
    }

    @Override
    public BigDecimal getOriginalPrice() {
        return new BigDecimal(originalEvent.getPrice());
    }

    @Override
    public BigDecimal getClonedPrice(int index) {
        return new BigDecimal(clonedEvents.get(index).getPrice());
    }

    @Override
    public BigDecimal getAverageClonedPrice() {
        BigDecimal avgPrice = new BigDecimal("0");
        for (int i = 0; i < clonedEvents.size(); i++) {
            avgPrice = avgPrice.add(getClonedPrice(i));
        }
        return clonedEvents.size() != 0 ? avgPrice.divide(new BigDecimal(clonedEvents.size()), MATH_CONTEXT) : avgPrice;
    }

    @Override
    public Long getDelay(int index) {
        return clonedEvents.get(index).getEventTime() - originalEvent.getEventTime();
    }

    @Override
    public Long getAverageDelay() {
        long avgDelay = 0L;
        for (int i = 0; i < clonedEvents.size(); i++) {
            avgDelay = avgDelay + getDelay(i);
        }
        return clonedEvents.size() != 0 ? avgDelay / clonedEvents.size() : avgDelay;
    }

    @Override
    public BigDecimal getOriginalIncome() {
        return null;
    }

    @Override
    public BigDecimal getClonedIncome(int index) {
        return null;
    }

    @Override
    public BigDecimal getAverageClonedIncome() {
        return null;
    }
}
*/
