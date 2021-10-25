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

        processor.run("Hello.jpg");
    }
}