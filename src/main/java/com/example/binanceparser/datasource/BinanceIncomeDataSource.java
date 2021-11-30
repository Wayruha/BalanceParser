package com.example.binanceparser.datasource;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.binance.BinanceClient;
import com.example.binanceparser.config.IncomeConfig;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public class BinanceIncomeDataSource implements EventSource<IncomeHistoryItem> {
    final String apiKey, secretKey;
    final IncomeConfig config;

    public BinanceIncomeDataSource(String apiKey, String secretKey, IncomeConfig config) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.config = config;
    }

    @Override
    public List<IncomeHistoryItem> getData() {
        final BinanceClient binanceClient = new BinanceClient(apiKey, secretKey);
        final Instant startTrackDate = config.getStartTrackDate().toInstant(ZoneOffset.of("+2"));
        final Instant finishTrackDate = config.getFinishTrackDate().toInstant(ZoneOffset.of("+2"));
        final List<IncomeHistoryItem> incomeList = binanceClient
                .fetchFuturesIncomeHistory(config.getSymbol(), config.getIncomeType(), startTrackDate, finishTrackDate, config.getLimit());
        return incomeList;
    }
}
