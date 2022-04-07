package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.FuturesStatsGenerationAlgorithm;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.StatsDataHolder;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.StatisticType;
import com.example.binanceparser.report.StatsReport;
import com.example.binanceparser.report.generator.StatsReportGenerator;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.binanceparser.report.StatisticType.*;

public class StatsProcessor {
    private StatsVisualizerConfig config;
    private EventSource<AbstractEvent> eventSource;
    private StatsReportGenerator reportGenerator;
    private FuturesStatsGenerationAlgorithm algorithm;
    private Set<StatisticType> statisticTypes = EnumSet.of(DELAY, ORDER_PRICE_DEVIATION, POSITION_PROFIT_DEVIATION);

    public StatsProcessor(StatsVisualizerConfig config, EventSource<AbstractEvent> eventSource) {
        this.config = config;
        this.eventSource = eventSource;
        this.reportGenerator = new StatsReportGenerator(config);
        this.algorithm = new FuturesStatsGenerationAlgorithm(config);
    }

    public List<StatsReport> process() {
        final List<AbstractEvent> data = eventSource.getData();
        StatsDataHolder stats = algorithm.getStats(data);

        return statisticTypes.stream()
                .map(type -> reportGenerator.getStatisticFor(stats, type))
                .collect(Collectors.toList());

    }
}
