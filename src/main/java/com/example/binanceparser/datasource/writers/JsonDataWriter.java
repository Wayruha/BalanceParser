package com.example.binanceparser.datasource.writers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Slf4j
public class JsonDataWriter<T> implements DataWriter<T> {
    private final OutputStream output;
    private final Class<T> type;
    private final ObjectMapper mapper;

    public JsonDataWriter(OutputStream output, Class<T> type) {
        this.output = output;
        this.type = type;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void write(List<T> items) {
        try {
            mapper.writerFor(type).writeValues(output).writeAll(items);
        } catch (IOException e) {
            log.warn("Exception writing data to json:" + e.getMessage());
        }
    }

    @Override
    public void write(T item) {
        //TODO
    }
}
