package com.example.binanceparser.datasource;

import com.binance.api.client.FuturesIncomeType;
import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.binance.BinanceClient;
import com.example.binanceparser.config.IncomeConfig;
import org.apache.commons.lang3.NotImplementedException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static java.time.Instant.ofEpochMilli;
import static java.util.Optional.ofNullable;

public class BinanceIncomeDataSource implements EventSource<IncomeHistoryItem> {
    final String apiKey, secretKey;
    final IncomeConfig config;
    final BinanceClient client;

    public BinanceIncomeDataSource(String apiKey, String secretKey, IncomeConfig config) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.config = config;
        this.client = new BinanceClient(apiKey, secretKey);
    }

    @Override
    public List<IncomeHistoryItem> getData() {
        final Instant startTrackDate = ofNullable(config.getStartTrackDate())
                .map(d -> d.toInstant(ZoneOffset.of("+2")))
                .orElse(null);
        final Instant finishTrackDate = ofNullable(config.getFinishTrackDate())
                .map(d -> d.toInstant(ZoneOffset.of("+2")))
                .orElse(null);
        final Set<IncomeHistoryItem> incomeList = new HashSet<>();
        for (FuturesIncomeType type : config.getIncomeTypes()) {
            final List<IncomeHistoryItem> items = fetchData(startTrackDate, finishTrackDate, type);
            incomeList.addAll(items);
        }
        final ArrayList<IncomeHistoryItem> incomeItems = new ArrayList<>(incomeList);
        incomeItems.sort(Comparator.comparing(IncomeHistoryItem::getTime));
        return incomeItems;
    }

    /**
     * if needed, it makes a few requests to ensure that whole date-range was covered
     */
    private List<IncomeHistoryItem> fetchData(Instant startDate, Instant endDate, FuturesIncomeType type) {
        List<IncomeHistoryItem> result = new ArrayList<>();
        List<IncomeHistoryItem> temp;
        do {
            temp = client.fetchFuturesIncomeHistory(config.getSymbol(),
                    type, startDate, endDate, config.getLimit());
            if(temp.size() == 0) return result;

            result.addAll(temp);
            startDate = ofEpochMilli(temp.get(temp.size() - 1 ).getTime() + 1);
        } while (temp.size() == config.getLimit() && startDate.compareTo(endDate) < 0);

        return result;
    }
}
