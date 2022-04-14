package com.example.binanceparser.datasource.sources;

import com.example.binanceparser.datasource.models.EventCSVModel;
import com.example.binanceparser.domain.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CSVEventSource implements DataSource<AbstractEvent> {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper;
    private final File csvFile;
    @Setter
    private List<String> trackedPersons;
    private CSVDataSource<EventCSVModel> csvSource;

    public CSVEventSource(File csvFile, List<String> trackedPersons) {
        this.objectMapper = new ObjectMapper();
        this.csvFile = csvFile;
        this.trackedPersons = trackedPersons;
        this.csvSource = new CSVDataSource<>(csvFile, 1, EventCSVModel.class);
    }

    @Override
    public List<AbstractEvent> getData() {
        final List<EventCSVModel> data = csvSource.getData();
        return data.stream()
                .filter(model -> trackedPersons.isEmpty() || trackedPersons.contains(model.getCustomer_id()))
                .map(this::modelToEvent)
                .collect(Collectors.toList());
    }

    //TODO не розбирався до кінця, але цей метод не повинен бути в CSV Event Source
    // він юзається в класах які по-ідеї не завязані КОНКРЕТНО на цю реалізацію, а через те що цей метод тут - вони тепер завязані на нього.
    public List<String> getUserIds() throws IllegalStateException, FileNotFoundException {
        final List<EventCSVModel> csvPojo = new CsvToBeanBuilder<EventCSVModel>(new FileReader(csvFile)).withType(EventCSVModel.class)
                .withSkipLines(1).build().parse();
        return csvPojo.stream()
                .map(EventCSVModel::getCustomer_id)
                .distinct()
                .collect(Collectors.toList());
    }


    private AbstractEvent modelToEvent(EventCSVModel model) {
        AbstractEvent event = null;
        try {
            if (model.getEvent_type().equals("FUTURES_ACCOUNT_UPDATE")) {
                event = objectMapper.readValue(model.getJson(), FuturesAccountUpdateEvent.class);
            } else if (model.getEvent_type().equals("FUTURES_ORDER_TRADE_UPDATE")) {
                event = objectMapper.readValue(model.getJson(), FuturesOrderTradeUpdateEvent.class);
            } else if (model.getEvent_type().equals("ACCOUNT_POSITION_UPDATE")) {
                event = objectMapper.readValue(model.getJson(), AccountPositionUpdateEvent.class);
            } else if (model.getEvent_type().equals("BALANCE_UPDATE")) {
                event = objectMapper.readValue(model.getJson(), BalanceUpdateEvent.class);
            } else if (model.getEvent_type().equals("ORDER_TRADE_UPDATE")) {
                event = objectMapper.readValue(model.getJson(), OrderTradeUpdateEvent.class);
            } else {
                event = new OtherEvents();
            }
            event.setDateTime(LocalDateTime.parse(model.getEvent_ts(), dateFormat));
            event.setEventType(EventType.valueOf(model.getEvent_type()));
            event.setSource(model.getCustomer_id());
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return event;
    }
}