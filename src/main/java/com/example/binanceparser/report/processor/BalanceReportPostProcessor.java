package com.example.binanceparser.report.processor;

import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.BalanceReport;

import java.util.List;

public class BalanceReportPostProcessor extends PostProcessor<AbstractEvent> {
    private final DataWriter<BalanceReport> dataWriter;

    public BalanceReportPostProcessor(DataWriter<BalanceReport> dataWriter) {
        this.dataWriter = dataWriter;
    }

    public void processReport(BalanceReport report, List<AbstractEvent> events) {
        dataWriter.writeEvents(List.of(report));
    }
}
