package com.example.binanceparser.report.postprocessor;

import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.AggregatedBalanceReport;
import com.example.binanceparser.report.BalanceReport;

import java.util.List;

public class AggregatedBalReportSerializer extends PostProcessor<AbstractEvent, AggregatedBalanceReport> {
    private final DataWriter<BalanceReport> dataWriter;

    public AggregatedBalReportSerializer(DataWriter<BalanceReport> dataWriter) {
        this.dataWriter = dataWriter;
    }

    public void processReport(AggregatedBalanceReport report, List<AbstractEvent> events) {
        dataWriter.write(report.getReports());
    }
}
