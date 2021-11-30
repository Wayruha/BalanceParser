package com.example.binanceparser.processor;

import com.example.binanceparser.Processor;
import com.example.binanceparser.algorithm.FuturesWalletBalanceCalcAlgorithm;
import com.example.binanceparser.config.Config;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.LogsEventSource;
import com.example.binanceparser.datasource.filters.DateEventFilter;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.BalanceReportGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BalanceStateProcessor extends Processor<BalanceVisualizerConfig> {
    final LogsEventSource eventSource;
    final BalanceReportGenerator balanceReportGenerator;
    final FuturesWalletBalanceCalcAlgorithm algorithm;

    public BalanceStateProcessor(BalanceVisualizerConfig config) {
        super(config);
        this.eventSource = new LogsEventSource();
        this.balanceReportGenerator = new BalanceReportGenerator(config);
        this.algorithm = new FuturesWalletBalanceCalcAlgorithm();
    }

    @Override
    public BalanceReport process() throws IOException {
        final File logsDir = new File(config.getInputFilepath());
        // read and filter events from data source
        List<AbstractEvent> events = new ArrayList<>(eventSource.readEvents(logsDir, implementFilters(config)));
        if (events.size() == 0) throw new RuntimeException("Can't find any relevant events");
        // retrieve balance changes
        final List<EventBalanceState> balanceStates = algorithm.processEvents(events, config.getAssetsToTrack());
        final BalanceReport balanceReport = balanceReportGenerator.getBalanceReport(balanceStates);

        System.out.println("Processor done for config: " + config);
        System.out.println("Report: " + balanceReport);
        return null;
    }

    private Set<Filter> implementFilters(Config config) {
        final BalanceVisualizerConfig balanceVisualizerConfig = (BalanceVisualizerConfig) config;
        final Set<Filter> filters = new HashSet<>();

        if (balanceVisualizerConfig.getStartTrackDate() != null || balanceVisualizerConfig.getFinishTrackDate() != null)
            filters.add(new DateEventFilter(balanceVisualizerConfig.getStartTrackDate(), balanceVisualizerConfig.getFinishTrackDate()));

        if (balanceVisualizerConfig.getSubject() != null) filters.add(new SourceFilter(balanceVisualizerConfig.getSubject()));

        if (balanceVisualizerConfig.getEventType() != null) filters.add(new EventTypeFilter(balanceVisualizerConfig.getEventType()));
        return filters;
    }
}
