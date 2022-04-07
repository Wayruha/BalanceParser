package com.example.binanceparser.report.processor;

import com.example.binanceparser.report.BalanceReport;

import java.util.List;

public interface PostProcessor<T> {
    BalanceReport processReport(BalanceReport report, List<T> events);
}
