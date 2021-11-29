package com.example.binanceparser;

import com.example.binanceparser.binance.BinanceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IncomeProcessorTest {

    @Test
    public void getchHistory() {
        BinanceClient client = new BinanceClient(Constants.BINANCE_API_KEY, Constants.BINANCE_SECRET_KEY);
        client.fetchFuturesIncomeHistory(null, null, null, null, 1000);
        System.out.println();
    }
}
