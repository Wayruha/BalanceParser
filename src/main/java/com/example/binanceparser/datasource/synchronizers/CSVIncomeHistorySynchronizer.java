package com.example.binanceparser.datasource.synchronizers;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.datasource.sources.DataSource;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
public class CSVIncomeHistorySynchronizer implements DataSynchronizer<IncomeHistoryItem> {
    private final List<String> persons;
    private final DataSource<IncomeHistoryItem> dataSource;
    private final File storedData;

    @Override
    public void synchronize() {
//        DataSource<IncomeHistoryItem> storedDataSource = new CSVIncomeSource(storedData, persons);
//        List<IncomeHistoryItem> storedEvents = storedDataSource.getData();
//        List<IncomeHistoryItem> newData = dataSource.getData();
//
//        DataWriter<IncomeHistoryItem> storedDataWriter = new CSVIncomeWriter(storedData, person);
//        if (storedEvents.size() > 0) {
//            IncomeHistoryItem lastStoredEvent = storedEvents.get(storedEvents.size() - 1);
//            newData = newData.stream().filter(event -> event.getTime() > lastStoredEvent.getTime()).collect(Collectors.toList());
//        }
//        storedDataWriter.write(newData);
    }
}
