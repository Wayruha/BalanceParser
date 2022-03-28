package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.Stats;
import com.example.binanceparser.domain.events.AbstractEvent;

import java.util.List;

public interface StatsGenerationAlgorithm<T extends Stats> {
    public List<T> getStats(List<AbstractEvent> events);
}
