package com.example.binanceparser.datasource;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.datasource.filters.DateIncomeFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonEventSource {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<IncomeHistoryItem> readEvents(File logsDir, DateIncomeFilter dateIncomeFilter) throws IOException {
        String[] dirFiles = logsDir.list();
        if (dirFiles == null) throw new RuntimeException("Can`t find any files in directory.");

        List<IncomeHistoryItem> incomes = new ArrayList<>();
        for (String filePath : dirFiles) {
            List<IncomeHistoryItem> newBalanceStates = Arrays.asList(objectMapper.readValue(
                    Paths.get(logsDir.getAbsolutePath() + "/" + filePath).toFile(), IncomeHistoryItem[].class));
            incomes.addAll(newBalanceStates);
        }
        return incomes;
    }
}
