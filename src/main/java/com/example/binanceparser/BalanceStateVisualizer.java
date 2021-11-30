package com.example.binanceparser;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.LogsEventSource;
import com.example.binanceparser.datasource.filters.DateEventFilter;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.processor.BalanceStateProcessor;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.binanceparser.Constants.BUSD;
import static com.example.binanceparser.Constants.USDT;

public class BalanceStateVisualizer {
    public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException {
        BalanceStateVisualizer app = new BalanceStateVisualizer();
        app.visualizeStateChangeFromLogs();
    }

    public void visualizeStateChangeFromLogs() throws IOException {
        final BalanceVisualizerConfig config = configure();
        final File logsDir = new File(config.getInputFilepath());
        final LogsEventSource logsEventSource = new LogsEventSource(logsDir, implementFilters(config));
        BalanceStateProcessor processor = new BalanceStateProcessor(logsEventSource, config);
        final BalanceReport report = processor.process();
        System.out.println("Report....");
        System.out.println(report.toPrettyString());
    }

    private static BalanceVisualizerConfig configure() {
        final BalanceVisualizerConfig config = new BalanceVisualizerConfig();
        LocalDateTime start = LocalDateTime.parse("2021-11-01 00:00:00", dateFormat);
        LocalDateTime finish = LocalDateTime.parse("2021-12-01 00:00:00", dateFormat);
        config.setStartTrackDate(start);
        config.setFinishTrackDate(finish);
        config.setSubject(List.of("FUTURES_rwdiachuk"));
        config.setInputFilepath("/Users/roman/Desktop/PassiveTrader/logs_01.12/events");
        config.setOutputDir("/Users/roman/Desktop");
        config.setAssetsToTrack(List.of(USDT, BUSD));
        config.setConvertToUSD(true);
        return config;
    }

    private static Set<Filter> implementFilters(BalanceVisualizerConfig config) {
        final Set<Filter> filters = new HashSet<>();
        if (config.getStartTrackDate() != null || config.getFinishTrackDate() != null)
            filters.add(new DateEventFilter(config.getStartTrackDate(), config.getFinishTrackDate()));

        if (config.getSubject() != null) filters.add(new SourceFilter(config.getSubject()));

        if (config.getEventType() != null) filters.add(new EventTypeFilter(config.getEventType()));
        return filters;
    }
}
