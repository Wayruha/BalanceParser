package com.example.binanceparser;

import com.example.binanceparser.algorithm.CalculationAlgorithm;
import com.example.binanceparser.algorithm.WalletBalanceCalcAlgorithm;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.filters.DateEventFilter;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.LogsEventSource;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.domain.AbstractEvent;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.domain.EventType;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.ReportGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            // read and filter events from data source
            List<AbstractEvent> events = new ArrayList<>(eventSource.readEvents(logsDir, implementFilters(config)));
            if (events.size() == 0) throw new RuntimeException("Can't find any relevant events");

            // retrieve balance changes
            final List<BalanceState> balanceStates = algorithm.processEvents(events, config.getAssetsToTrack());

            final BalanceReport balanceReport = reportGenerator.getBalanceReport(config, balanceStates);

            System.out.println("Processor done for config: " + config);
            return balanceReport;
        }

        //TODO 1. Що буде якщо startDate == 10/01/2021 а endDate=null
        private Set<Filter> implementFilters(Config config){
            Set<Filter> filters = new HashSet<>();
            if(config.getStartTrackDate() != null || config.getFinishTrackDate() != null)
                filters.add(new DateEventFilter(config.getStartTrackDate(), config.getFinishTrackDate()));

            if(config.getSourceToTrack() != null) filters.add(new SourceFilter(config.getSourceToTrack()));

            if(config.getEventType() != null) filters.add(new EventTypeFilter(EventType.valueOf(config.getEventType())));

            return filters;
        }
}
