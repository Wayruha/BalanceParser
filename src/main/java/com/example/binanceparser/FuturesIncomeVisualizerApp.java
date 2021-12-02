package com.example.binanceparser;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.BinanceIncomeDataSource;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.JsonIncomeSource;
import com.example.binanceparser.processor.IncomeProcessor;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FuturesIncomeVisualizerApp {
    static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        final FuturesIncomeVisualizerApp app = new FuturesIncomeVisualizerApp();
        final BalanceReport balanceReport = app.loadDataFromBinance();
        System.out.println("Report....");
        System.out.println(balanceReport.toPrettyString());
    }

    private static IncomeConfig configure() {
        IncomeConfig config = new IncomeConfig();
        LocalDateTime start = LocalDateTime.parse("2021-08-30 06:17:56", dateFormat);
        LocalDateTime finish = LocalDateTime.parse("2021-11-30 13:15:50", dateFormat);
        config.setStartTrackDate(start);
        config.setFinishTrackDate(finish);
        config.setOutputDir("С:/users/yarik/Desktop");
        //if we want to read data from filesystem
        //config.setInputFilepath("");
        return config;
    }

    public BalanceReport loadDataFromBinance() {
        final IncomeConfig config = configure();
        config.setLimit(1000);
        config.setSubject(List.of("RDiachuk"));
        EventSource<IncomeHistoryItem> apiClientSource = new BinanceIncomeDataSource(Constants.BINANCE_API_KEY, Constants.BINANCE_SECRET_KEY, config);
        IncomeProcessor processor = new IncomeProcessor(apiClientSource, config);
        return processor.process();
    }

    public BalanceReport loadFromLogs() {
        final IncomeConfig config = configure();
        final File logsDir = new File(config.getInputFilepath());
        final EventSource<IncomeHistoryItem> incomeSource = new JsonIncomeSource(logsDir);
        final IncomeProcessor processor = new IncomeProcessor(incomeSource, config);
        return processor.process();
    }
}
