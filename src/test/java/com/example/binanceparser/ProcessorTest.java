package com.example.binanceparser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ProcessorTest {

    final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testProcessor() throws IOException {
        Processor processor = new Processor();
        Config config = new Config();

        //LocalDateTime start = LocalDateTime.parse("2021-08-30 06:17:56", dateFormat);
        //LocalDateTime finish = LocalDateTime.parse("2021-09-15 13:15:50", dateFormat);
        //config.setStartTrackDate(start);
        //config.setFinishTrackDate(finish);
        //List<String> assetsToTrack = new ArrayList<>();
        //assetsToTrack.add("USDT");
        //assetsToTrack.add("BUSD");
        //config.setAssetsToTrack(assetsToTrack);
        config.setSourceToTrack(List.of("FUTURES_PRODUCER_Kozhukhar", "SPOT_PRODUCER_Kozhukhar"));
        config.setInputFilepath("src/main/resources/log");
        config.setOutputDir("C:/Users/yarik/Desktop");
        config.setConvertToUSD(true);
        //config.setEventType(List.of(EventType.FUTURES_ACCOUNT_UPDATE));
        System.out.println(processor.run(config));
    }
}
