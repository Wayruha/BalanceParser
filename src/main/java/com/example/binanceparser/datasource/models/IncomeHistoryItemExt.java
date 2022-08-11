package com.example.binanceparser.datasource.models;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.datasource.Readable;
import com.example.binanceparser.datasource.Writable;

import java.util.List;
import java.util.stream.Collectors;

public class IncomeHistoryItemExt extends IncomeHistoryItem implements Writable, Readable {
    @Override
    public String header() {
        return null;
    }

    @Override
    public String csv() {
        return null;
    }

    public static List<IncomeHistoryItemExt> wrap(List<IncomeHistoryItem> incomes) {
        return incomes.stream().map(IncomeHistoryItemExt::wrap).collect(Collectors.toList());
    }

    public static IncomeHistoryItemExt wrap(IncomeHistoryItem item) {
        IncomeHistoryItemExt ext = new IncomeHistoryItemExt();
        ext.setSymbol(item.getSymbol());
        ext.setIncomeType(item.getIncomeType());
        ext.setIncome(item.getIncome());
        ext.setAsset(item.getAsset());
        ext.setInfo(item.getInfo());
        ext.setTime(item.getTime());
        ext.setTranId(item.getTranId());
        ext.setTradeId(item.getTradeId());
        return ext;
    }
}
