/*
package com.example.binanceparser;

import com.example.binanceparser.algorithm.FuturesStatsGenerationAlgorithm;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.events.FuturesOrderTradeUpdateEvent;
import com.example.binanceparser.statistics.StatsProcessor;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.statistics.StatsReport;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class StatsVisualizer {
    public static void main(String[] args) throws IOException {
        String trader = "nefedov";
        StatsVisualizer visualizer = new StatsVisualizer();
        visualizer.visualizeStats(trader);
    }

    public void visualizeStats(String trader) throws IOException {
        StatsVisualizerConfig config = configure();
        File logsDir = new File(config.getInputFilepath());
        CSVEventSource eventSource = new CSVEventSource(logsDir, List.of(trader));
        StatsProcessor processor = new StatsProcessor(config);
        StatsReport report = processor.process();
        System.out.println("Report....");
        System.out.println(report.toPrettyString());
    }

    private static StatsVisualizerConfig configure() {
        StatsVisualizerConfig config = new StatsVisualizerConfig();
        //TODO
        return config;
    }
}
*/
