package com.example.binanceparser.datasource.synchronizers;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.datasource.models.IncomeHistoryItemExt;
import com.example.binanceparser.datasource.sources.CSVDataSource;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.writers.CSVDataWriter;
import com.example.binanceparser.datasource.writers.DataWriter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CSVIncomeHistorySynchronizer implements DataSynchronizer<IncomeHistoryItem> {
    private final DataSource<IncomeHistoryItem> dataSource;
    private final File storedData;

    @Override
    public void synchronize() throws FileNotFoundException {
        DataSource<IncomeHistoryItemExt> storedDataSource = new CSVDataSource<>(storedData, IncomeHistoryItemExt.class);
        List<IncomeHistoryItemExt> storedEvents = storedDataSource.getData();
        List<IncomeHistoryItemExt> newData = IncomeHistoryItemExt.wrap(dataSource.getData());

        DataWriter<IncomeHistoryItemExt> storedDataWriter = new CSVDataWriter<>(new FileOutputStream(storedData), IncomeHistoryItemExt.class);
        if (storedEvents.size() > 0) {
            IncomeHistoryItem lastStoredEvent = storedEvents.get(storedEvents.size() - 1);
            newData = newData.stream().filter(event -> event.getTime() > lastStoredEvent.getTime()).collect(Collectors.toList());
        }
        storedDataWriter.write(newData);
    }
}
