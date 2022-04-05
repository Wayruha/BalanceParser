package com.example.binanceparser.domain;

import com.example.binanceparser.Constants;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.*;

@Data
@AllArgsConstructor
public class Stats {
    private final Map<ComplexEvent, List<ComplexEvent>> data;
    private final StatsVisualizerConfig config;

    public Stats(StatsVisualizerConfig config) {
        this.config = config;
        this.data = new HashMap<>();
    }

    public Map<BigDecimal, Integer> getPriceDataset() {
        Map<BigDecimal, Integer> dataset = new HashMap<>();
        data.keySet().forEach(originalEvent ->
                data.get(originalEvent).forEach(clonedEvent -> {
                    final BigDecimal deviation = getRelativePriceDeviation(originalEvent.getFilledEvent(), clonedEvent.getFilledEvent());
                    final int occur = dataset.getOrDefault(deviation, 0);
                    dataset.put(deviation, occur + 1);
                })
        );
        return dataset;
    }

    public BigDecimal getAveragePriceDeviation() {
        BigDecimal value = ZERO;
        int count = 0;
        for (ComplexEvent originalEvent : data.keySet()) {
            for (ComplexEvent clonedEvent : data.get(originalEvent)) {
                value = value.add(getRelativePriceDeviation(originalEvent.getFilledEvent(), clonedEvent.getFilledEvent()));
                count++;
            }
        }
        return count == 0 ? ZERO : value.divide(valueOf(count), Constants.MATH_CONTEXT);
    }

    private BigDecimal getRelativePriceDeviation(FuturesOrderTradeUpdateEvent originalEvent, FuturesOrderTradeUpdateEvent clonedEvent) {
        return clonedEvent.getPriceOfLastFilledTrade()
                .divide(originalEvent.getPriceOfLastFilledTrade(), config.getRelativeContext())
                .subtract(ONE);
    }

    public Map<BigDecimal, Integer> getDelayDataset() {
        Map<BigDecimal, Integer> dataset = new HashMap<>();
        data.keySet().forEach((originalEvent) ->
                data.get(originalEvent).forEach((clonedEvent) -> {
                    BigDecimal deviation = getDelayDeviation(originalEvent.getFirstValuableEvent(), clonedEvent.getFirstValuableEvent());
                    int occur = dataset.getOrDefault(deviation, 0);
                    dataset.put(deviation, occur + 1);
                })
        );
        return dataset;
    }

    public BigDecimal getAverageDelayDeviation() {
        BigDecimal value = ZERO;
        int count = 0;
        for (ComplexEvent originalEvent : data.keySet()) {
            for (ComplexEvent clonedEvent : data.get(originalEvent)) {
                value = value.add(getDelayDeviation(originalEvent.getFirstValuableEvent(), clonedEvent.getFirstValuableEvent()));
                count++;
            }
        }
        return count == 0 ? ZERO : value.divide(valueOf(count), Constants.MATH_CONTEXT);
    }

    private BigDecimal getDelayDeviation(FuturesOrderTradeUpdateEvent originalEvent, FuturesOrderTradeUpdateEvent clonedEvent) {
        long result = (clonedEvent.getEventTime() - originalEvent.getEventTime())
                - (clonedEvent.getEventTime() - originalEvent.getEventTime()) % config.getDelayPrecision();
        return valueOf(result);
    }

    public Map<BigDecimal, Integer> getIncomeDataset() {
        Map<BigDecimal, Integer> dataset = new HashMap<>();
        data.keySet().forEach((originalEvent) ->
                data.get(originalEvent).forEach((clonedEvent) -> {
                    BigDecimal deviation = getRelativeIncomeDeviation(originalEvent.getFilledEvent(), clonedEvent.getFilledEvent());
                    int occur = dataset.getOrDefault(deviation, 0);
                    dataset.put(deviation, occur + 1);
                })
        );
        return dataset;
    }

    public BigDecimal getAverageIncomeDeviation() {
        BigDecimal value = ZERO;
        int count = 0;
        for (ComplexEvent originalEvent : data.keySet()) {
            for (ComplexEvent clonedEvent : data.get(originalEvent)) {
                value = value.add(getRelativeIncomeDeviation(originalEvent.getFilledEvent(), clonedEvent.getFilledEvent()));
                count++;
            }
        }
        return count == 0 ? ZERO : value.divide(valueOf(count), Constants.MATH_CONTEXT);
    }

    private BigDecimal getRelativeIncomeDeviation(FuturesOrderTradeUpdateEvent originalEvent, FuturesOrderTradeUpdateEvent clonedEvent) {
        BigDecimal quoteTraderQty = originalEvent.getAveragePrice().multiply(originalEvent.getOriginalQuantity());
        BigDecimal relativeTraderIncome = originalEvent.getRealizedTradeProfit().divide(quoteTraderQty, config.getRelativeContext());
        BigDecimal quoteConsumerQty = clonedEvent.getAveragePrice().multiply(clonedEvent.getOriginalQuantity());
        BigDecimal relativeConsumerIncome = clonedEvent.getRealizedTradeProfit().divide(quoteConsumerQty, config.getRelativeContext());
        return relativeConsumerIncome.subtract(relativeTraderIncome);
    }

    public void insertRecord(ComplexEvent originalEvent, List<ComplexEvent> clonedEvents) {
        data.put(originalEvent, clonedEvents);
    }
}
