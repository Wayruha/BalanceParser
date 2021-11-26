package com.example.binanceparser;

import com.example.binanceparser.algorithm.IncomeCalculationAlgorithm;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.JsonEventSource;
import com.example.binanceparser.datasource.filters.DateEventFilter;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.domain.Income;
import com.example.binanceparser.domain.IncomeBalanceState;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.ReportGenerator;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Processor {
        final JsonEventSource eventSource;
        //final CalculationAlgorithm algorithm;
        final ReportGenerator reportGenerator;

        public Processor() {
            this.eventSource = new JsonEventSource();
            //this.algorithm = new SpotBalanceCalcAlgorithm();
            this.reportGenerator = new ReportGenerator();
        }

        public BalanceReport run(Config config) throws IOException {
            final File logsDir = new File(config.getInputFilepath());
            // read and filter events from data source
            //List<AbstractEvent> events = new ArrayList<>(eventSource.readEvents(logsDir, implementFilters(config)));

            List<Income> incomes = eventSource.readEvents(logsDir, implementFilters(config));
            if (incomes.size() == 0) throw new RuntimeException("Can't find any relevant events");

            // retrieve balance changes
            IncomeCalculationAlgorithm jsonCalculationAlgorithm = new IncomeCalculationAlgorithm();
            final List<IncomeBalanceState> logBalanceStates = jsonCalculationAlgorithm.calculateBalance(incomes);

            final BalanceReport balanceReport = reportGenerator.getBalanceReport(config, logBalanceStates);

            System.out.println("Processor done for config: " + config);
            return balanceReport;
        }

        private Set<Filter> implementFilters(Config config){
            Set<Filter> filters = new HashSet<>();
            if(config.getStartTrackDate() != null || config.getFinishTrackDate() != null)
                filters.add(new DateEventFilter(config.getStartTrackDate(), config.getFinishTrackDate()));

            if(config.getSourceToTrack() != null) filters.add(new SourceFilter(config.getSourceToTrack()));

            if(config.getEventType() != null) filters.add(new EventTypeFilter(config.getEventType()));

            return filters;
        }
}
