package com.example.binanceparser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProcessorTest {

    @Test
    public void testProcessor() throws IOException {
        Processor processor = new Processor();
        Processor.Config config = new Processor.Config();
        config.setSourceToTrack("FUTURES_PRODUCER_Kozhukhar");
        config.setInputFilepath("/Users/roman/Desktop/BinanceParser/logs");
        config.setOutputDir("/Users/roman/Desktop");
        processor.run(config);
    }
}
