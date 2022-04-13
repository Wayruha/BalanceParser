package com.example.binanceparser.datasource.writers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Slf4j
public class CSVDataWriter<T> implements DataWriter<T> {
    private final OutputStream output;
    private final Class<T> type;
    private final CsvMapper mapper;

    public CSVDataWriter(OutputStream output, Class<T> type) {
        this.output = output;
        this.type = type;
        mapper = new CsvMapper();
    }

    @Override
    public void writeEvents(List<T> items) {
        try {
            CsvSchema schema = getSchema();
            mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
            mapper.writerFor(type).with(schema).writeValues(output).writeAll(items);
        } catch (IOException e) {
            log.warn("Exception writing data to csv:" + e.getMessage());
        }
    }

    protected CsvSchema getSchema() {
        return CsvSchema.emptySchema();
    }
}
