package com.example.binanceparser.datasource.writers;

import com.example.binanceparser.datasource.Readable;
import com.example.binanceparser.datasource.Writable;
import com.opencsv.bean.FuzzyMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
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
    public void write(List<T> items) {
        try (PrintWriter writer = new PrintWriter(output)) {
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer)
                    .withApplyQuotesToAll(false)
                    .withMappingStrategy(getStrategy())
                    .build();
            beanToCsv.write(items);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.warn("Exception writing data to csv:" + e.getMessage());
        }
       /* try (PrintWriter pw = new PrintWriter(output)) {
            if (empty && !items.isEmpty()) {
                pw.write(items.get(0).header());
            } else if (!empty && !items.isEmpty()) {
                //check header match
            }
            items.stream().forEach(item -> pw.write(item.csv()));
        }*/
    }

    private FuzzyMappingStrategy<T> getStrategy() {
        MultiValuedMap<Class<?>, Field> map = new ArrayListValuedHashMap<>();
        Arrays.stream(type.getFields()).filter(field -> Modifier.isTransient(field.getModifiers())).forEach(field -> map.put(type, field));
        FuzzyMappingStrategy<T> mappingStrategy = new FuzzyMappingStrategy<>();
        mappingStrategy.setType(type);
        mappingStrategy.ignoreFields(map);
        return mappingStrategy;
    }

    @Override
    public void write(T item) {
        write(List.of(item));
    }
}
