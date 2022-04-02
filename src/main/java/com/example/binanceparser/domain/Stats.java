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
    private final Map<FuturesOrderTradeUpdateEvent, List<FuturesOrderTradeUpdateEvent>> data;
    private final StatsVisualizerConfig config;

    public Stats(StatsVisualizerConfig config) {
        this.config = config;
        data = new HashMap<>();
    }

    public Map<BigDecimal, Integer> getPriceDataset() {
        Map<BigDecimal, Integer> dataset = new HashMap<>();
        data.keySet().forEach((originalEvent) -> {
            data.get(originalEvent).forEach((clonedEvent) -> {
                BigDecimal deviation = getRelativePriceDeviation(originalEvent, clonedEvent);
                Integer occur = !dataset.containsKey(deviation) ? 0 : dataset.get(deviation);
                dataset.put(deviation, occur + 1);
            });
        });
        return dataset;
    }

    public BigDecimal getAveragePriceDeviation() {
        BigDecimal value = ZERO;
        int count = 0;
        for (FuturesOrderTradeUpdateEvent originalEvent : data.keySet()) {
            for (FuturesOrderTradeUpdateEvent clonedEvent : data.get(originalEvent)) {
                value = value.add(getRelativePriceDeviation(originalEvent, clonedEvent));
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
        data.keySet().forEach((originalEvent) -> {
            data.get(originalEvent).forEach((clonedEvent) -> {
                BigDecimal deviation = getDelayDeviation(originalEvent, clonedEvent);
                Integer occur = !dataset.containsKey(deviation) ? 0 : dataset.get(deviation);
                dataset.put(deviation, occur + 1);
            });
        });
        return dataset;
    }

    public BigDecimal getAverageDelayDeviation() {
        BigDecimal value = ZERO;
        int count = 0;
        for (FuturesOrderTradeUpdateEvent originalEvent : data.keySet()) {
            for (FuturesOrderTradeUpdateEvent clonedEvent : data.get(originalEvent)) {
                value = value.add(getDelayDeviation(originalEvent, clonedEvent));
                count++;
            }
        }
        return count == 0 ? ZERO : value.divide(valueOf(count), Constants.MATH_CONTEXT);
    }

    private BigDecimal getDelayDeviation(FuturesOrderTradeUpdateEvent originalEvent, FuturesOrderTradeUpdateEvent clonedEvent) {
        Long result = (clonedEvent.getEventTime() - originalEvent.getEventTime())
                - (clonedEvent.getEventTime() - originalEvent.getEventTime()) % config.getDelayPrecision();
        return valueOf(result);
    }

    public Map<BigDecimal, Integer> getIncomeDataset() {
        Map<BigDecimal, Integer> dataset = new HashMap<>();
        data.keySet().forEach((originalEvent) -> {
            data.get(originalEvent).forEach((clonedEvent) -> {
                BigDecimal deviation = getRelativeIncomeDeviation(originalEvent, clonedEvent);
                Integer occur = !dataset.containsKey(deviation) ? 0 : dataset.get(deviation);
                dataset.put(deviation, occur + 1);
            });
        });
        return dataset;
    }

    public BigDecimal getAverageIncomeDeviation() {
        BigDecimal value = ZERO;
        int count = 0;
        for (FuturesOrderTradeUpdateEvent originalEvent : data.keySet()) {
            for (FuturesOrderTradeUpdateEvent clonedEvent : data.get(originalEvent)) {
                value = value.add(getRelativeIncomeDeviation(originalEvent, clonedEvent));
                count++;
            }
        }
        return count == 0 ? ZERO : value.divide(valueOf(count), Constants.MATH_CONTEXT);
    }

    private BigDecimal getRelativeIncomeDeviation(FuturesOrderTradeUpdateEvent originalEvent, FuturesOrderTradeUpdateEvent clonedEvent) {
        return clonedEvent.getRealizedTradeProfit()
                .divide(originalEvent.getRealizedTradeProfit(), config.getRelativeContext())
                .subtract(ONE);
    }

    public void insertRecord(FuturesOrderTradeUpdateEvent originalEvent, List<FuturesOrderTradeUpdateEvent> clonedEvents) {
        data.put(originalEvent, clonedEvents);
    }
}
