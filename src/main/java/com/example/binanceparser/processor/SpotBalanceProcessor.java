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
        List<EventBalanceState> balanceStates = algorithm
        		.processEvents(events)
        		.stream()
        		.filter(event -> inRange(event, config.getStartTrackDate(), config.getFinishTrackDate()))
        		.collect(Collectors.toList());
        
        final BalanceReport balanceReport = balanceReportGenerator.getBalanceReport(balanceStates);

        System.out.println("Processor done for config: " + config);
        return balanceReport;
    }

    private boolean inRange(EventBalanceState state, LocalDateTime rangeStart, LocalDateTime rangeEnd){
        return state.getDateTime().compareTo(rangeStart) >= 0
                && state.getDateTime().compareTo(rangeEnd) <= 0;
    }
}
