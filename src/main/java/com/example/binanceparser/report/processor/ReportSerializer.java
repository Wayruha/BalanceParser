package com.example.binanceparser.report.processor;

import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.domain.events.AbstractEvent;

import java.util.List;

public class ReportSerializer<T> extends PostProcessor<AbstractEvent, T> {
    private final DataWriter<T> dataWriter;

    public ReportSerializer(DataWriter<T> dataWriter) {
        this.dataWriter = dataWriter;
    }

    public void processReport(T report, List<AbstractEvent> events) {
        dataWriter.writeEvents(List.of(report));
    }
}
