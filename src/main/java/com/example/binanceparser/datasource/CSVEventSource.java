package com.example.binanceparser.datasource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.domain.events.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		List<AbstractEvent> events = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(csvDir))) {
			String line;
			while ((line = br.readLine()) != null) {
				Matcher m = Pattern.compile("(\\w+),([\\d\\-\\s:]+),(.+)").matcher(line);
				AbstractEvent event = objectMapper.readValue(m.group(3), AbstractEvent.class);
				event.setEventType(EventType.valueOf(m.group(1)));
				event.setDateTime(LocalDateTime.parse(m.group(2), dateFormat));
				events.add(event);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return events;
	}
}