package com.example.binanceparser.processor;

import com.example.binanceparser.algorithm.SpotBalanceCalcAlgorithm;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.domain.EventBalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.plot.AssetChartBuilder;
import com.example.binanceparser.plot.ChartBuilder;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.BalanceReportGenerator;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

//TODO its mostly duplicate of BalanceStateProcessor.
//TODO need to combine it after we handle Deposit/Withdraw delta feature
public class SpotBalanceProcessor extends Processor<BalanceVisualizerConfig> {
    protected final EventSource<AbstractEvent> eventSource;
    private final BalanceReportGenerator balanceReportGenerator;
    private final SpotBalanceCalcAlgorithm algorithm;

    public SpotBalanceProcessor(EventSource<AbstractEvent> eventSource, BalanceVisualizerConfig config) {
        super(config);
        this.eventSource = eventSource;
        final ChartBuilder<EventBalanceState> chartBuilder = new AssetChartBuilder(config.getAssetsToTrack());
        this.balanceReportGenerator = new BalanceReportGenerator(config, chartBuilder);
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
        	.filter(state -> inRange(state.getDateTime(), config.getStartTrackDate(), config.getFinishTrackDate()))
        	.collect(Collectors.toList());

        final BalanceReport balanceReport = balanceReportGenerator.getBalanceReport(balanceStates);

        System.out.println("Processor done for config: " + config);
        return balanceReport;
    }

    protected boolean inRange(LocalDateTime date, LocalDateTime rangeStart, LocalDateTime rangeEnd){
        return date.compareTo(rangeStart) >= 0 && date.compareTo(rangeEnd) <= 0;
    }
}
