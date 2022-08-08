package com.example.binanceparser.datasource.synchronizers;

import com.example.binanceparser.datasource.sources.CSVEventSource;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.writers.CSVEventWriter;
import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.domain.events.AbstractEvent;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CSVEventSynchronizer implements DataSynchronizer<AbstractEvent> {
    private final List<String> persons;
    private final DataSource<AbstractEvent> dataSource;
    private final File storedData;

    @Override
    public void synchronize() {
        DataSource<AbstractEvent> storedDataSource = new CSVEventSource(storedData, persons);
        List<AbstractEvent> storedEvents = storedDataSource.getData();
        List<AbstractEvent> newData = dataSource.getData();

        for (String person : persons(newData)) {
            DataWriter<AbstractEvent> storedDataWriter = new CSVEventWriter(storedData, person);
            List<AbstractEvent> personalizedEvents = storedEvents.stream().filter(event -> event.getSource().equals(person)).collect(Collectors.toList());
            List<AbstractEvent> personalizedNewData = newData.stream().filter(event -> event.getSource().equals(person)).collect(Collectors.toList());
            if (personalizedEvents.size() > 0) {
                AbstractEvent lastStoredEvent = personalizedEvents.get(personalizedEvents.size() - 1);
                personalizedNewData = personalizedNewData.stream().filter(event -> event.getEventTime().compareTo(lastStoredEvent.getEventTime()) > 0).collect(Collectors.toList());
            }
            storedDataWriter.write(personalizedNewData);
        }
    }

    private List<String> persons(List<AbstractEvent> events) {
        return events.stream().map(AbstractEvent::getSource).distinct()
                .filter(source -> persons.isEmpty() || persons.contains(source)).collect(Collectors.toList());
    }
}