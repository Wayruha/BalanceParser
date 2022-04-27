package com.example.binanceparser.processor;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.datasource.sources.InMemorySource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryHistoryItemSource extends InMemorySource<IncomeHistoryItem> {
    public InMemoryHistoryItemSource(Stream<IncomeHistoryItem> stream) {
        super(stream);
    }

    @Override
    public List<IncomeHistoryItem> getData() {
        return stream.collect(Collectors.toList());
    }
}
