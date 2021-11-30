package com.example.binanceparser.plot;

import com.example.binanceparser.domain.BalanceState;
import org.jfree.chart.JFreeChart;

import java.util.List;

public interface ChartBuilder<T extends BalanceState> {

    JFreeChart buildLineChart(List<T> logBalanceStates);
}
