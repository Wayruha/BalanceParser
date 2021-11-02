package com.example.binanceparser.algorithm;

import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;

import java.util.List;

/**
 * takes a full list of events and produces the extract balance-change data
 */
public interface CalculationAlgorithm {
    List<BalanceState> processEvents(List<AbstractEvent> events);
}
