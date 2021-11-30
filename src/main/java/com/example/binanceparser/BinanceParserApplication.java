package com.example.binanceparser;

import com.example.binanceparser.config.IncomeConfig;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BinanceParserApplication {
    final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException {
        BinanceParserApplication app = new BinanceParserApplication();
        app.visualizeIncome();
    }

    public void visualizeIncome() throws IOException {
        FuturesBalanceIncomeProcessor futuresBalanceIncomeProcessor = new FuturesBalanceIncomeProcessor();
        IncomeConfig config = new IncomeConfig();

        LocalDateTime start = LocalDateTime.parse("2021-01-15 06:17:56", dateFormat);
        LocalDateTime finish = LocalDateTime.parse("2021-11-30 13:15:50", dateFormat);
        config.setStartTrackDate(start);
        config.setFinishTrackDate(finish);
        config.setLogProducer(List.of("FUTURES_PRODUCER_Kozhukhar"));
        //config.setInputFilepath("logs");
//      config.setInputFilepath("/Users/roman/Desktop/passiveTrader_events");
        //config.setOutputDir("/Users/roman/Desktop");
        config.setOutputDir("C:\\Users\\yarik\\Desktop");
        System.out.println(futuresBalanceIncomeProcessor.run(config));
    }
}
