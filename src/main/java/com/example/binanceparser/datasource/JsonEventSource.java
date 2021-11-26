package com.example.binanceparser.datasource;

import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.domain.Income;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class JsonEventSource{

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Income> readEvents(File logsDir, Set<Filter> filters) throws IOException {
        String[] dirFiles = logsDir.list();
        if (dirFiles == null) throw new RuntimeException("Can`t find any files in directory.");

        List<Income> incomes = new ArrayList<>();
        for (String filePath : dirFiles) {
            List<Income> newBalanceStates = Arrays.asList(objectMapper.readValue(
                    Paths.get(logsDir.getAbsolutePath() + "/" + filePath).toFile(), Income[].class));
            incomes.addAll(newBalanceStates);
        }
        return incomes;
    }
}
