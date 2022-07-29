package com.example.binanceparser.binance;

import com.binance.api.client.ApiRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.FuturesIncomeType;
import com.binance.api.client.FuturesRestClient;
import com.binance.api.client.domain.account.request.FuturesAccountInfo;
import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.binance.api.client.domain.general.ExchangeInfo;

import java.time.Instant;
import java.util.List;

public class BinanceClient {

    private final String apiKey;
    private final String secretKey;
    private final BinanceApiClientFactory clientFactory;
    protected final FuturesRestClient restClient;

    public BinanceClient(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        clientFactory = new BinanceApiClientFactory(apiKey, secretKey, false);
        restClient = clientFactory.newFuturesRestClient();
    }

    public List<IncomeHistoryItem> fetchFuturesIncomeHistory(String symbol, FuturesIncomeType incomeType, Instant startTime, Instant endTime, int limit) {
        final List<IncomeHistoryItem> incomeHistory = restClient.getIncomeHistory(symbol, incomeType, startTime, endTime, limit);
        return incomeHistory;
    }

    public ExchangeInfo loadExchangeInfo() {
        final ApiRestClient dummyClient = new BinanceApiClientFactory(null, null, false).newRestClient();
        return dummyClient.getExchangeInfo();
    }

    public FuturesAccountInfo getAccountInfo() {
        return restClient.getAccountInfo();
    }
}
