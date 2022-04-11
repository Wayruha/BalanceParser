package com.example.binanceparser.datasource.writers;

import com.example.binanceparser.report.BalanceReport;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Slf4j
public class CSVBalanceReportWriter implements DataWriter<BalanceReport> {
    private final OutputStream output;

    public CSVBalanceReportWriter(OutputStream output) {
        this.output = output;
    }

    @Override
    public void writeEvents(List<BalanceReport> items) {
        try {
            CsvMapper mapper = new CsvMapper();
            mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
            mapper.writerFor(BalanceReport.class).writeValues(output).writeAll(items);
        } catch (IOException e) {
            log.warn("Exception writing balance report:" + e.getMessage());
        }
    }
}
