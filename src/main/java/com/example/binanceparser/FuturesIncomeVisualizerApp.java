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

import static com.binance.api.client.FuturesIncomeType.REALIZED_PNL;
import static java.time.LocalDateTime.parse;
import static java.util.List.of;

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
        LocalDateTime start = parse("2021-11-30 00:00:00", dateFormat);
        LocalDateTime finish = parse("2021-12-31 23:50:50", dateFormat);
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
        config.setSubject(of("RDiachuk"));
        config.setIncomeType(of(REALIZED_PNL));
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
