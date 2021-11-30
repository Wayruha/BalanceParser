package com.example.binanceparser;

import com.example.binanceparser.config.IncomeConfig;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BinanceParserApplication {
    final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException {
        BinanceParserApplication app = new BinanceParserApplication();
        app.visualizeIncome();
    }

    public void visualizeIncome() throws IOException {
        final Config config = new IncomeConfig();
        LocalDateTime start = LocalDateTime.parse("2021-01-30 06:17:56", dateFormat);
        LocalDateTime finish = LocalDateTime.parse("2021-11-15 13:15:50", dateFormat);
        config.setStartTrackDate(start);
        config.setFinishTrackDate(finish);
        //config.setInputFilepath("src/main/resources/testJsonLog");
        config.setOutputDir("/Users/roman/Desktop");
        IncomeProcessor incomeProcessor = new IncomeProcessor(null, config); //TODO not null should be here!
        System.out.println(incomeProcessor.process());
    }
}
