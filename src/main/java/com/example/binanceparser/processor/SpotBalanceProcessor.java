package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.SpotBalanceCalcAlgorithm;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.BalanceReportGenerator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

//TODO its mostly duplicate of BalanceStateProcessor.
//TODO need to combine it after we handle Deposit/Withdraw delta feature
public class SpotBalanceProcessor extends Processor<BalanceVisualizerConfig> {
    final EventSource<AbstractEvent> eventSource;
    final BalanceReportGenerator balanceReportGenerator;
    final SpotBalanceCalcAlgorithm algorithm;

    public SpotBalanceProcessor(EventSource<AbstractEvent> eventSource, BalanceVisualizerConfig config) {
        super(config);
        this.eventSource = eventSource;
        this.balanceReportGenerator = new BalanceReportGenerator(config);
        this.algorithm = new SpotBalanceCalcAlgorithm(config);
    }

    @Override
    public BalanceReport process() throws IOException {
        final List<AbstractEvent> events = eventSource.getData();
        if (events.size() == 0) throw new RuntimeException("Can't find any relevant events");
        // retrieve balance changes
        List<EventBalanceState> balanceStates = algorithm.processEvents(events, config.getAssetsToTrack());

        balanceStates = filter(balanceStates, config.getStartTrackDate(), config.getFinishTrackDate());
        final BalanceReport balanceReport = balanceReportGenerator.getBalanceReport(balanceStates);

        System.out.println("Processor done for config: " + config);
        return balanceReport;
    }

    private List<EventBalanceState> filter(List<EventBalanceState> events, LocalDateTime startDate, LocalDateTime endDate) {
        return events.stream()
                .filter(
                        event -> event.getDateTime().compareTo(startDate) >= 0
                                && event.getDateTime().compareTo(endDate) <= 0)
                .collect(Collectors.toList());
    }
}
