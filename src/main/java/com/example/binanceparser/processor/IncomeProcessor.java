package com.example.binanceparser.processor;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.algorithm.IncomeCalculationAlgorithm;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.balance.IncomeBalanceState;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.generator.IncomeReportGenerator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class IncomeProcessor extends Processor<IncomeConfig> {
    private static final Logger log = Logger.getLogger(IncomeProcessor.class.getName());

    final EventSource<IncomeHistoryItem> eventSource;
    final IncomeReportGenerator reportGenerator;
    final IncomeCalculationAlgorithm algorithm;
    final IncomeConfig config;

    public IncomeProcessor(EventSource<IncomeHistoryItem> eventSource, IncomeConfig config) {
        super(config);
        this.eventSource = eventSource;
        this.config = config;
        this.reportGenerator = new IncomeReportGenerator(config);
        this.algorithm = new IncomeCalculationAlgorithm();
    }

    @Override
    public BalanceReport process() {
        try {
            final List<IncomeHistoryItem> incomes = eventSource.getData();
            if (incomes.size() == 0) throw new RuntimeException("No data!");
            Collections.sort(incomes, Comparator.comparing(IncomeHistoryItem::getTime));

            final List<IncomeBalanceState> states = algorithm.calculateBalance(incomes);
            final IncomeBalanceState emptyState = new IncomeBalanceState(config.getStartTrackDate(), BigDecimal.ZERO, config.getIncomeTypes().get(0));
            final BalanceReport balanceReport = reportGenerator.getBalanceReport(states);
            log.info("FuturesIncomeProcessor done for config: " + config);
            return balanceReport;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new BalanceReport();
    }
}