package com.example.binanceparser.report;

import lombok.Getter;

import java.util.List;

@Getter
public class AggregatedBalanceReport {
    final List<BalanceReport> reports;
    int itemsCount;
    //todo totalBalance
    // totalProfit etc.

    public AggregatedBalanceReport(List<BalanceReport> reports) {
        this.reports = reports;
    }
}
