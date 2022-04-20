package com.example.binanceparser.statistics;

import com.example.binanceparser.algorithm.FuturesStatsGenerationAlgorithm;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.binanceparser.statistics.StatisticType.*;

@Service
class StatsProcessor {
    private DataSource<AbstractEvent> eventSource;
    private StatsReportGenerator reportGenerator;
    private FuturesStatsGenerationAlgorithm algorithm;
    private Set<StatisticType> statisticTypes = EnumSet.of(DELAY, ORDER_PRICE_DEVIATION, POSITION_PROFIT_DEVIATION);

    public StatsProcessor(StatsVisualizerConfig config, DataSource<AbstractEvent> eventSource,
                          StatsReportGenerator reportGenerator, FuturesStatsGenerationAlgorithm algorithm) {
        this.eventSource = eventSource;
        this.reportGenerator = reportGenerator;//new StatsReportGenerator(config);
        this.algorithm = algorithm;//new FuturesStatsGenerationAlgorithm(config);
    }

    public List<StatsReport> process() {
        final List<AbstractEvent> data = eventSource.getData();
        StatsDataHolder stats = algorithm.getStats(data);

        return statisticTypes.stream()
                .map(type -> reportGenerator.getStatisticFor(stats, type))
                .collect(Collectors.toList());

    }
}
