package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.StatsDataHolder;
import com.example.binanceparser.domain.events.AbstractEvent;

import java.util.List;

public interface StatsGenerationAlgorithm<T extends StatsDataHolder> {
    T getStats(List<AbstractEvent> events);
}
