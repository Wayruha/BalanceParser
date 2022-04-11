package com.example.binanceparser.datasource.sources;

import com.example.binanceparser.datasource.models.UserName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;

@Slf4j
public class CSVUserNameSource implements DataSource<UserName> {
    private final ObjectMapper objectMapper;
    private final File csvDir;

    public CSVUserNameSource(File csvDir) {
        this.csvDir = csvDir;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<UserName> getData() {
        try {
            return new CsvToBeanBuilder<UserName>(new FileReader(csvDir)).withType(UserName.class)
                    .withSkipLines(1).build().parse();
        } catch (FileNotFoundException e) {
            log.warn(e.getMessage());
        }
        return Collections.emptyList();
    }
}
