package com.example.binanceparser.processor;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.algorithm.IncomeCalculationAlgorithm;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.domain.balance.IncomeBalanceState;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.generator.IncomeReportGenerator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class IncomeProcessor extends Processor<IncomeHistoryItem, BalanceReport> {
    private static final Logger log = Logger.getLogger(IncomeProcessor.class.getName());

    private final IncomeReportGenerator reportGenerator;
    private final IncomeCalculationAlgorithm algorithm;
    private final IncomeConfig config;

    public IncomeProcessor(DataSource<IncomeHistoryItem> eventSource, IncomeConfig config) {
        super(eventSource);
        this.config = config;
        this.reportGenerator = new IncomeReportGenerator(config);
        this.algorithm = new IncomeCalculationAlgorithm();
    }

    @Override
    protected BalanceReport process(List<IncomeHistoryItem> incomes) {
        try {
            incomes = incomes.stream()
                    .filter(item -> config.getAssetsToTrack().isEmpty() || config.getAssetsToTrack().contains(item.getAsset()))
                    .collect(Collectors.toList());
            if (incomes.size() == 0) throw new RuntimeException("No data!");
            Collections.sort(incomes, Comparator.comparing(IncomeHistoryItem::getTime));

            final List<IncomeBalanceState> states = algorithm.calculateBalance(incomes);
            final BalanceReport balanceReport = reportGenerator.getBalanceReport(states);
            log.info("FuturesIncomeProcessor done for config: " + config);
            return balanceReport;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new BalanceReport();
    }
}