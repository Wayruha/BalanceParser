package com.example.binanceparser.domain;

import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ONE;

@Data
@AllArgsConstructor
public class StatsDataHolder {
    private final Map<ComplexEvent, List<ComplexEvent>> data;
    private final StatsVisualizerConfig config;

    public StatsDataHolder(StatsVisualizerConfig config) {
        this.config = config;
        this.data = new HashMap<>();
    }

    public void insertRecord(ComplexEvent originalEvent, List<ComplexEvent> clonedEvents) {
        data.put(originalEvent, clonedEvents);
    }

    public List<Integer> getDelayData() {
        List<Integer> dataList = new ArrayList<>();
        data.forEach((originalEvent, subscriberEvents) -> {
            subscriberEvents.forEach(clonedEvent -> {
                final int cloningDelay = (int) (clonedEvent.getFirstValuableEvent().getEventTime() - originalEvent.getFirstValuableEvent().getEventTime());
                dataList.add(cloningDelay);
            });
        });
        return dataList;
    }

    public List<Double> getOrderPriceData() {
        final List<Double> dataList = new ArrayList<>();
        data.forEach((originalEvent, subscriberEvents) -> {
            subscriberEvents.forEach(clonedEvent -> {
                final double priceDeviation = getRelativePriceDeviation(originalEvent.getFilledEvent(), clonedEvent.getFilledEvent()).doubleValue();
                dataList.add(priceDeviation);
            });
        });
        return dataList;
    }

    public List<Double> getPositionProfitData() {
        List<Double> dataList = new ArrayList<>();
        data.forEach((originalEvent, subscriberEvents) -> {
            subscriberEvents.forEach(clonedEvent -> {
                final double incomeDeviation = getRelativeIncomeDeviation(originalEvent.getFilledEvent(), clonedEvent.getFilledEvent()).doubleValue();
                dataList.add(incomeDeviation);
            });
        });
        return dataList;
    }

    private BigDecimal getRelativePriceDeviation(FuturesOrderTradeUpdateEvent originalEvent, FuturesOrderTradeUpdateEvent clonedEvent) {
        return clonedEvent.getPriceOfLastFilledTrade()
                .divide(originalEvent.getPriceOfLastFilledTrade(), config.getRelativeContext())
                .subtract(ONE);
    }

    private BigDecimal getRelativeIncomeDeviation(FuturesOrderTradeUpdateEvent originalEvent, FuturesOrderTradeUpdateEvent clonedEvent) {
        BigDecimal quoteTraderQty = originalEvent.getAveragePrice().multiply(originalEvent.getOriginalQuantity());
        BigDecimal relativeTraderIncome = originalEvent.getRealizedTradeProfit().divide(quoteTraderQty, config.getRelativeContext());
        BigDecimal quoteConsumerQty = clonedEvent.getAveragePrice().multiply(clonedEvent.getOriginalQuantity());
        BigDecimal relativeConsumerIncome = clonedEvent.getRealizedTradeProfit().divide(quoteConsumerQty, config.getRelativeContext());
        return relativeConsumerIncome.subtract(relativeTraderIncome);
    }
}
