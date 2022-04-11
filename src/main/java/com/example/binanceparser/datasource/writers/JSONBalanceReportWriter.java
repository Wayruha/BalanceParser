package com.example.binanceparser.datasource.writers;

import com.example.binanceparser.report.BalanceReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Slf4j
public class JSONBalanceReportWriter implements DataWriter<BalanceReport> {
    private final ObjectMapper mapper;
    private final OutputStream output;

    public JSONBalanceReportWriter(OutputStream output) {
        this.output = output;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void writeEvents(List<BalanceReport> items) {
        try {
            mapper.writerFor(BalanceReport.class).writeValues(output).writeAll(items);
        } catch (IOException e) {
            log.warn("Exception writing balance report:" + e.getMessage());
        }
    }
}
