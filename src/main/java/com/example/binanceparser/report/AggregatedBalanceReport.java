package com.example.binanceparser.report;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class AggregatedBalanceReport {
    private final List<BalanceReport> reports;
    //todo totalBalance
    // totalProfit etc.

    public AggregatedBalanceReport(List<BalanceReport> reports) {
        this.reports = reports;
    }

    public int getItemsCount() {
        return reports.size();
    }

    public BigDecimal getBalanceAtStart() {
        return reports.stream().map(report -> report.getBalanceAtStart()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getBalanceAtEnd() {
        return reports.stream().map(report -> report.getBalanceAtEnd()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
