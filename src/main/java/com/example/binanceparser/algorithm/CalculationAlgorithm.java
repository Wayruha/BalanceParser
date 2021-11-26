package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.EventBalanceState;

import java.util.List;

/**
 * takes a full list of events and produces the extract balance-change data
 */
public interface CalculationAlgorithm {
    List<EventBalanceState> processEvents(List<AbstractEvent> events, List<String> assetsToTrack);
}
