package com.example.binanceparser.report.processor;

import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.report.BalanceReport;

import java.util.List;

public class BalanceReportPostProcessor {
    private final DataWriter<BalanceReport> reportWriter;

    public BalanceReportPostProcessor(DataWriter<BalanceReport> reportWriter) {
        this.reportWriter = reportWriter;
    }

    public void processReport(BalanceReport report, List<BalanceReport> events) {

    }
}
