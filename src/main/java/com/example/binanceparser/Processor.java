package com.example.binanceparser;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.algorithm.IncomeCalculationAlgorithm;
import com.example.binanceparser.binance.BinanceClient;
import com.example.binanceparser.datasource.JsonEventSource;
import com.example.binanceparser.datasource.filters.*;
import com.example.binanceparser.domain.Income;
import com.example.binanceparser.domain.IncomeBalanceState;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.IncomeReportGenerator;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Processor {
    final JsonEventSource eventSource;
    final IncomeReportGenerator reportGenerator;

        public Processor() {
            this.eventSource = new JsonEventSource();
            this.reportGenerator = new IncomeReportGenerator();
        }

        public BalanceReport run(Config config) throws IOException {

            if(config.getInputFilepath() == null){
                final BinanceClient binanceClient = new BinanceClient(Constants.BINANCE_API_KEY, Constants.BINANCE_SECRET_KEY);
                final Instant startTrackDate = config.getStartTrackDate().toInstant(ZoneOffset.of("+2"));
                final Instant finishTrackDate = config.getFinishTrackDate().toInstant(ZoneOffset.of("+2"));
                final List<IncomeHistoryItem> incomeList = binanceClient
                        .fetchFuturesIncomeHistory(null, null, startTrackDate, finishTrackDate, 1000);
                final List<IncomeBalanceState> balanceStates = IncomeCalculationAlgorithm.calculateBalance(incomeList);
                final BalanceReport balanceReport = reportGenerator.getBalanceReport(config, balanceStates);
                return balanceReport;
            }
            else {
                final File logsDir = new File(config.getInputFilepath());

                List<IncomeHistoryItem> incomes = eventSource.readEvents(logsDir, new DateIncomeFilter(config.getStartTrackDate(), config.getFinishTrackDate()));
                if (incomes.size() == 0) throw new RuntimeException("Can't find any relevant events");

                final List<IncomeBalanceState> logBalanceStates = IncomeCalculationAlgorithm.calculateBalance(incomes);

                final BalanceReport balanceReport = reportGenerator.getBalanceReport(config, logBalanceStates);

                System.out.println("Processor done for config: " + config);
                return balanceReport;
            }
        }

        private Set<Filter> implementFilters(Config config){
            EventConfig eventConfig = (EventConfig) config;
            Set<Filter> filters = new HashSet<>();

            if(eventConfig.getStartTrackDate() != null || eventConfig.getFinishTrackDate() != null)
                filters.add(new DateEventFilter(eventConfig.getStartTrackDate(), eventConfig.getFinishTrackDate()));

            if(eventConfig.getSourceToTrack() != null) filters.add(new SourceFilter(eventConfig.getSourceToTrack()));

            if(eventConfig.getEventType() != null) filters.add(new EventTypeFilter(eventConfig.getEventType()));


            return filters;
        }
}
