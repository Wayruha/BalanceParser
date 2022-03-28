package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.FuturesStatsGenerationAlgorithm;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.report.StatsReport;
import com.example.binanceparser.report.StatsReportGenerator;

import java.io.IOException;

public class StatsProcessor {
    private StatsVisualizerConfig config;
    private StatsReportGenerator reportGenerator;
    private FuturesStatsGenerationAlgorithm algorithm;

    public StatsProcessor(StatsVisualizerConfig config) {
        this.config = config;
        reportGenerator = new StatsReportGenerator();
        algorithm = new FuturesStatsGenerationAlgorithm(config);
    }

    public StatsReport process() throws IOException {
        StatsReport report = reportGenerator.getStatsReport();
        return report;
    }
}
