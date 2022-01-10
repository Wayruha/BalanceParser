package com.example.binanceparser.datasource;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.example.binanceparser.domain.events.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class CSVEventSource implements EventSource<AbstractEvent> {
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private ObjectMapper objectMapper;
	private File csvDir;

	public CSVEventSource(File csvDir) {
		objectMapper = new ObjectMapper();
		this.csvDir = csvDir;
	}

	@Override
	public List<AbstractEvent> getData() {
		List<AbstractEvent> events = null;
		try {
			CsvSchema schema = CsvSchema.builder()
					.addColumn("event_type")
					.addColumn("event_ts")
					.addColumn("json")
					.build().withHeader();
			MappingIterator<CSVModel> iterator = new CsvMapper().readerFor(CSVModel.class).with(schema)
					//here is some problem
					.readValues(csvDir);
			events = iterator.readAll().stream().map((model) -> modelToEvent(model)).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return events;
	}
	
	private AbstractEvent modelToEvent(CSVModel model) {
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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		event.setDateTime(LocalDateTime.parse(model.getEvent_ts(), dateFormat));
		event.setEventType(EventType.valueOf(model.getEvent_type()));
		return event;
	}
}