package com.example.binanceparser.datasource;

import com.example.binanceparser.domain.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CSVEventSource implements EventSource<AbstractEvent> {
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final ObjectMapper objectMapper;
	private final File csvDir;
	private final String trackedPerson;

	public CSVEventSource(File csvDir, String trackedPerson) {
		this.objectMapper = new ObjectMapper();
		this.csvDir = csvDir;
		this.trackedPerson = trackedPerson;
	}

	@Override
	public List<AbstractEvent> getData() {
		try {
			final List<CSVModel> csvPojo = getCsvPojo();
			return csvPojo.stream()
					.filter(model -> model.getCustomer_id().equals(trackedPerson))
					.map(this::modelToEvent)
					.collect(Collectors.toList());
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}

	public List<String> getuserIds() throws IllegalStateException, FileNotFoundException {
		return getCsvPojo().stream()
				.map(CSVModel::getCustomer_id)
				.distinct()
				.collect(Collectors.toList());
	}

	private List<CSVModel> getCsvPojo() throws IllegalStateException, FileNotFoundException {
		final List<CSVModel> csvPojo = new CsvToBeanBuilder<CSVModel>(new FileReader(csvDir)).withType(CSVModel.class)
				.withSkipLines(1).build().parse();
		return csvPojo;
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
			event.setDateTime(LocalDateTime.parse(model.getEvent_ts(), dateFormat));
			event.setEventType(EventType.valueOf(model.getEvent_type()));
			event.setSource(trackedPerson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;
	}
}