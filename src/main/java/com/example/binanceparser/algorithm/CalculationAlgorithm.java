package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;

import java.util.List;

/**
 * takes a full list of events and produces the extract balance-change data
 */
public interface CalculationAlgorithm<T extends BalanceState> {
    List<T> processEvents(List<AbstractEvent> events, List<String> assetsToTrack);
    //returns processEvents(List<AbstractEvent> events, config.getAsssetsToTrack)
    //in case of call from somewhere except BalanceStateVisualizer(assetsToTrack already = config.getAsssetsToTrack)
    //added 
    List<T> processEvents(List<AbstractEvent> events);
}
