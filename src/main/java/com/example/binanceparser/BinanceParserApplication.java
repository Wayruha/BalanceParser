package com.example.binanceparser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BinanceParserApplication {
    final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException {
        BinanceParserApplication app = new BinanceParserApplication();
        app.visualizeIncome();
    }

    public void visualizeIncome() throws IOException {
        Processor processor = new Processor();
        Config config = new Config();

        LocalDateTime start = LocalDateTime.parse("2021-08-15 06:17:56", dateFormat);
        LocalDateTime finish = LocalDateTime.parse("2021-11-30 13:15:50", dateFormat);
        config.setStartTrackDate(start);
        config.setFinishTrackDate(finish);
        List<String> assetsToTrack = new ArrayList<>();
        assetsToTrack.add("USDT");
        assetsToTrack.add("BUSD");
        config.setAssetsToTrack(assetsToTrack);
        config.setSourceToTrack(List.of("FUTURES_PRODUCER_Kozhukhar"));
        config.setInputFilepath("logs");

//        config.setInputFilepath("/Users/roman/Desktop/passiveTrader_events");
        config.setOutputDir("/Users/roman/Desktop");
        config.setConvertToUSD(true);
        //config.setEventType(List.of(EventType.FUTURES_ACCOUNT_UPDATE));
        System.out.println(processor.run(config));
    }
}
