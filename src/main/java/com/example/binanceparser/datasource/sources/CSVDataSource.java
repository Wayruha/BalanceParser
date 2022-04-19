package com.example.binanceparser.datasource.sources;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class CSVDataSource<T> implements DataSource<T> {
    private final File inputFilePath;
    private final int skipLines;
    private final Class<T> type;

    public CSVDataSource(File inputFilePath, int skipLines, Class<T> type) {
        this.inputFilePath = inputFilePath;
        this.skipLines = skipLines;
        this.type = type;
    }

    public CSVDataSource(File inputFilePath, Class<T> type) {
        this.inputFilePath = inputFilePath;
        this.skipLines = 0;
        this.type = type;
    }

    @Override
    public List<T> getData() {
        try {
            final FileReader reader = new FileReader(inputFilePath);
            return new CsvToBeanBuilder<T>(reader)
                    .withType(type)
                    .withSkipLines(skipLines)
                    .build().parse();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
