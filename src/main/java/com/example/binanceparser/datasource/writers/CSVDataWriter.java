package com.example.binanceparser.datasource.writers;

import com.example.binanceparser.datasource.Readable;
import com.example.binanceparser.datasource.Writable;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

@Slf4j
public class CSVDataWriter<T extends Writable & Readable> implements DataWriter<T> {
    private final OutputStream output;
    private final Class<T> type;
    private final boolean empty;

    public CSVDataWriter(OutputStream output, Class<T> type, boolean append) {
        this.output = output;
        this.type = type;
        this.empty = append;
    }

    @Override
    public void writeEvents(List<T> items) {
//        OutputStreamWriter writer = new OutputStreamWriter(output);
//
//        StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer).build();
//        try {
//            beanToCsv.write(items);
//            writer.close();
//        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
//            log.warn("Exception writing data to csv:" + e.getMessage());
//        }
        try (PrintWriter pw = new PrintWriter(output)) {
            if (empty && !items.isEmpty()) {
                pw.write(items.get(0).header());
            } else if (!empty && !items.isEmpty()) {
                //check header match
            }
            items.stream().forEach(item -> pw.write(item.csv()));
        }
    }
}
