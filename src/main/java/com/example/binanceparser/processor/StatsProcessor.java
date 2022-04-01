package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.FuturesStatsGenerationAlgorithm;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.domain.Stats;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.StatsReport;
import com.example.binanceparser.report.StatsReportGenerator;

import java.io.IOException;
import java.util.List;

public class StatsProcessor {
    private StatsVisualizerConfig config;
    private List<AbstractEvent> eventSource;
    private StatsReportGenerator reportGenerator;
    private FuturesStatsGenerationAlgorithm algorithm;

    public StatsProcessor(StatsVisualizerConfig config, List<AbstractEvent> eventSource) {
        this.config = config;
        this.eventSource = eventSource;
        reportGenerator = new StatsReportGenerator(config);
        algorithm = new FuturesStatsGenerationAlgorithm(config);
    }

    public StatsReport process() throws IOException {
        Stats stats = algorithm.getStats(eventSource);
        StatsReport report = reportGenerator.getStatsReport(stats);
        return report;
    }
}
