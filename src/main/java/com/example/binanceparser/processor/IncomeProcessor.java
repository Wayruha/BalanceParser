package com.example.binanceparser.processor;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.Processor;
import com.example.binanceparser.algorithm.IncomeCalculationAlgorithm;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.IncomeBalanceState;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.IncomeReportGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IncomeProcessor extends Processor<IncomeConfig> {
    final EventSource<IncomeHistoryItem> eventSource;
    final IncomeReportGenerator reportGenerator;
    final IncomeCalculationAlgorithm algorithm;
    final IncomeConfig config;

    public IncomeProcessor(EventSource<IncomeHistoryItem> eventSource, IncomeConfig config) {
        super(config);
        this.eventSource = eventSource;
        this.config = config;
        this.reportGenerator = new IncomeReportGenerator();
        this.algorithm = new IncomeCalculationAlgorithm();
    }

    @Override
    public BalanceReport process() {
        try {
            final List<IncomeHistoryItem> incomes = eventSource.getData();
            if (incomes.size() == 0) throw new RuntimeException("No data!");
            Collections.sort(incomes, Comparator.comparing(IncomeHistoryItem::getTime));

            final List<IncomeBalanceState> logBalanceStates = algorithm.calculateBalance(incomes);
            final BalanceReport balanceReport = reportGenerator.getBalanceReport(config, logBalanceStates);
            System.out.println("Processor done for config: " + config);
            return balanceReport;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    //TODO нам потрібен цей код? На роботі, таке ніколи не повинно потрапляти в кодову базу. В КРАЙНЬОМУ разів - має бути поясненнся
      /*  private Set<Filter> implementFilters(Config config){
            EventConfig eventConfig = (EventConfig) config;
            Set<Filter> filters = new HashSet<>();

            if(eventConfig.getStartTrackDate() != null || eventConfig.getFinishTrackDate() != null)
                filters.add(new DateEventFilter(eventConfig.getStartTrackDate(), eventConfig.getFinishTrackDate()));

            if(eventConfig.getSourceToTrack() != null) filters.add(new SourceFilter(eventConfig.getSourceToTrack()));

            if(eventConfig.getEventType() != null) filters.add(new EventTypeFilter(eventConfig.getEventType()));


            return filters;
        }*/
}
