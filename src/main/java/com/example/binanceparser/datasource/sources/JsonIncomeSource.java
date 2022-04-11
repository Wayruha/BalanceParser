package com.example.binanceparser.datasource.sources;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.datasource.sources.EventSource;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonIncomeSource implements DataSource<IncomeHistoryItem> {

    private final ObjectMapper objectMapper;
    private final File logsDir;

    public JsonIncomeSource(File logsDir) {
        this.objectMapper = new ObjectMapper();
        this.logsDir = logsDir;
    }

    @Override
    public List<IncomeHistoryItem> getData() {
        String[] dirFiles = logsDir.list();
        if (dirFiles == null) throw new RuntimeException("Can`t find any files in directory.");

        List<IncomeHistoryItem> incomes = new ArrayList<>();
        for (String filePath : dirFiles) {
            List<IncomeHistoryItem> newBalanceStates = null;
            try {
                newBalanceStates = Arrays.asList(objectMapper.readValue(
                        Paths.get(logsDir.getAbsolutePath() + "/" + filePath).toFile(), IncomeHistoryItem[].class));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            incomes.addAll(newBalanceStates);
        }
        return incomes;
    }
}
