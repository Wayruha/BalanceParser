package com.example.binanceparser;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.processor.FuturesBalanceStateProcessor;
import com.example.binanceparser.processor.TestSpotBalanceProcessor;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.binanceparser.Constants.*;

public class BalanceStateVisualizer {

    public static void main(String[] args) throws IOException {
        BalanceStateVisualizer app = new BalanceStateVisualizer();
        final String person = "nefedov";
        app.futuresStateChangeFromLogs(person);
//		app.spotStateChangeFromLogs(person);
    }

    public void futuresStateChangeFromLogs(String person) throws IOException {
        final BalanceVisualizerConfig config = configure();
        config.setSubject(List.of(person));
        config.setAssetsToTrack(List.of());
//        addSubject(config, person, "FUTURES_PRODUCER");

        final File logsDir = new File(config.getInputFilepath());
        final CSVEventSource logsEventSource = new CSVEventSource(logsDir, person);
        FuturesBalanceStateProcessor processor = new FuturesBalanceStateProcessor(logsEventSource, config);
        final BalanceReport report = processor.process();
        System.out.println("Report....");
        System.out.println(report.toPrettyString());
    }

    public void spotStateChangeFromLogs(String person) throws IOException {
        final BalanceVisualizerConfig config = configure();
        config.setSubject(List.of(person));
        config.setAssetsToTrack(List.of(VIRTUAL_USD));

//        addSubject(config, person, "SPOT_PRODUCER");

        final File logsDir = new File(config.getInputFilepath());
        final CSVEventSource logsEventSource = new CSVEventSource(logsDir, person);
//		SpotBalanceProcessor processor = new SpotBalanceProcessor(logsEventSource, config);
        TestSpotBalanceProcessor testProcessor = new TestSpotBalanceProcessor(logsEventSource, config);
//		final BalanceReport report = processor.process();
        final BalanceReport testReport = testProcessor.process();
//		System.out.println("Report....");
//		System.out.println(report.toPrettyString());
        System.out.println("Test report....");
        System.out.println(testReport.toPrettyString());
    }

    private static BalanceVisualizerConfig configure() {
        final BalanceVisualizerConfig config = new BalanceVisualizerConfig();
        LocalDateTime start = LocalDateTime.parse("2021-08-16 00:00:00", DATE_FORMAT);
        LocalDateTime finish = LocalDateTime.parse("2021-12-30 00:00:00", DATE_FORMAT);
        config.setStartTrackDate(start);
        config.setFinishTrackDate(finish);
        config.setInputFilepath("C:/Users/Sanya/Desktop/ParserOutput/trader_events.csv");
        config.setOutputDir("C:/Users/Sanya/Desktop/ParserOutput");
        //config.setAssetsToTrack(List.of(USDT, BUSD, BTC, ETH, AXS));
        config.setAssetsToTrack(Collections.emptyList());
        config.setConvertToUSD(false);
        return config;
    }

    private static void addSubject(BalanceVisualizerConfig config, String subject, String prefix) {
        final String resolvedSubjectName = prefix + "_" + subject;
        final List<String> list = config.getSubject();
        if (list == null) {
            config.setSubject(new ArrayList<>());
        }
        config.getSubject().add(resolvedSubjectName);
    }

    private static Set<Filter> filters(BalanceVisualizerConfig config) {
        final Set<Filter> filters = new HashSet<>();
        if (config.getSubject() != null) {
            filters.add(new SourceFilter(config.getSubject()));
        }

        if (config.getEventType() != null) {
            filters.add(new EventTypeFilter(config.getEventType()));
        }
        return filters;
    }
}