package com.example.binanceparser;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.algorithm.IncomeCalculationAlgorithm;
import com.example.binanceparser.binance.BinanceClient;
import com.example.binanceparser.config.Config;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.JsonEventSource;
import com.example.binanceparser.datasource.filters.DateIncomeFilter;
import com.example.binanceparser.domain.IncomeBalanceState;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.IncomeReportGenerator;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public class FuturesBalanceIncomeProcessor implements Processor {
    final JsonEventSource eventSource;
    final IncomeReportGenerator reportGenerator;

    public FuturesBalanceIncomeProcessor() {
        this.eventSource = new JsonEventSource();
        this.reportGenerator = new IncomeReportGenerator();
    }

    public BalanceReport run(Config config) throws IOException {
        IncomeConfig incomeConfig = (IncomeConfig) config;
        if (config.getInputFilepath() == null) {
            final BinanceClient binanceClient = new BinanceClient(Constants.BINANCE_API_KEY, Constants.BINANCE_SECRET_KEY);
            final Instant startTrackDate = incomeConfig.getStartTrackDate().toInstant(ZoneOffset.of("+2"));
            final Instant finishTrackDate = incomeConfig.getFinishTrackDate().toInstant(ZoneOffset.of("+2"));
            final List<IncomeHistoryItem> incomeList = binanceClient
                    .fetchFuturesIncomeHistory(null, null, startTrackDate, finishTrackDate, 1000);
            final List<IncomeBalanceState> balanceStates = IncomeCalculationAlgorithm.calculateBalance(incomeList);
            final BalanceReport balanceReport = reportGenerator.getBalanceReport(incomeConfig, balanceStates);
            return balanceReport;
        } else {
            final File logsDir = new File(config.getInputFilepath());

            List<IncomeHistoryItem> incomes = eventSource.readEvents(logsDir, new DateIncomeFilter(incomeConfig.getStartTrackDate(), incomeConfig.getFinishTrackDate()));
            if (incomes.size() == 0) throw new RuntimeException("Can't find any relevant events");

            final List<IncomeBalanceState> logBalanceStates = IncomeCalculationAlgorithm.calculateBalance(incomes);

            final BalanceReport balanceReport = reportGenerator.getBalanceReport(incomeConfig, logBalanceStates);

            System.out.println("Processor done for config: " + incomeConfig);
            return balanceReport;
        }
    }
}
