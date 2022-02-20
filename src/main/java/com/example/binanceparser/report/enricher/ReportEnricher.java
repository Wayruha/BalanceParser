package com.example.binanceparser.report.enricher;

import com.example.binanceparser.report.BalanceReport;

import java.util.List;

public interface ReportEnricher<T> {

    BalanceReport enrichReport(BalanceReport report, List<T> events);
}
