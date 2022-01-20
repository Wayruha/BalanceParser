package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.TestSpotBalanceCalcAlgorithm;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.SpotIncomeState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.plot.TestAssetChartBuilder;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.TestBalanceReportGenerator;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TestSpotBalanceProcessor extends SpotBalanceProcessor {

    private final TestBalanceReportGenerator balanceReportGenerator;
    private final TestSpotBalanceCalcAlgorithm algorithm;

    public TestSpotBalanceProcessor(EventSource<AbstractEvent> eventSource, BalanceVisualizerConfig config) {
        super(eventSource, config);
        final TestAssetChartBuilder chartBuilder = new TestAssetChartBuilder(config.getAssetsToTrack());
        this.balanceReportGenerator = new TestBalanceReportGenerator(config, chartBuilder);
        this.algorithm = new TestSpotBalanceCalcAlgorithm();
    }

    @Override
    public BalanceReport process() throws IOException {
        final List<AbstractEvent> events = eventSource.getData();
        if (events.size() == 0)
            throw new RuntimeException("Can't find any relevant events");
        // retrieve balance changes
        List<SpotIncomeState> balanceStates = algorithm.processEvents(events).stream()
                .filter(state -> inRange(state.getDateTime(), config.getStartTrackDate(), config.getFinishTrackDate()))
                .collect(Collectors.toList());

        final BalanceReport balanceReport = balanceReportGenerator.getBalanceReport(balanceStates);

        final List<AbstractEvent> periodRelevantEvents = events.stream().filter(e -> inRange(e.getDateTime(), config.getStartTrackDate(), config.getFinishTrackDate()))
                .collect(Collectors.toList());
        System.out.println("Transferred to Futures: " + FuturesBalanceStateProcessor.calculateDepositDelta(periodRelevantEvents));
        System.out.println("Processor done for config: " + config);
        return balanceReport;
    }
}