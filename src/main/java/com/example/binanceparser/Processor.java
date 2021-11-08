package com.example.binanceparser;

import com.example.binanceparser.algorithm.CalculationAlgorithm;
import com.example.binanceparser.algorithm.WalletBalanceCalcAlgorithm;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.Filter;
import com.example.binanceparser.datasource.LogsEventSource;
import com.example.binanceparser.datasource.SourceFilter;
import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.ReportGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Processor {
        final EventSource eventSource;
        final CalculationAlgorithm algorithm;
        final ReportGenerator reportGenerator;

        public Processor() {
            this.eventSource = new LogsEventSource();
            this.algorithm = new WalletBalanceCalcAlgorithm();
            this.reportGenerator = new ReportGenerator();
        }

        public BalanceReport run(Config config) throws IOException {
            final File logsDir = new File(config.getInputFilepath());
            Filter sourceFilter = new SourceFilter(config.getSourceToTrack());
            // read and filter events from data source
            List<AbstractEvent> events = eventSource.readEvents(logsDir, List.of(sourceFilter)).stream()
                    .filter(event -> event.getDate().isAfter(config.startTrackDate) && event.getDate().isBefore(config.getFinishTrackDate()))
                    .collect(Collectors.toList());
            if (events.size() == 0) throw new RuntimeException("Can't find any relevant events");

            // retrieve balance changes
            final List<BalanceState> balanceStates = algorithm.processEvents(events, config.getAssetsToTrack());

            final BalanceReport balanceReport = reportGenerator.getBalanceReport(config, balanceStates);

            System.out.println("Processor done for config: " + config);
            return balanceReport;
        }
}
