package com.example.binanceparser.datasource;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.example.binanceparser.domain.events.AbstractEvent;

public class CSVEventWriter implements EventWriter<AbstractEvent> {
	private File outputDir;

	public CSVEventWriter(File outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public void writeEvents(List<AbstractEvent> events) {
		try (PrintWriter pw = new PrintWriter(outputDir)) {
			events.stream().forEach((event) -> {
				StringBuilder sb = new StringBuilder();
				try {
					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String json = ow.writeValueAsString(event);
					sb.append(event.getEventType()).append(",").append(event.getDateTime().toString()).append(",")
							.append(json);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				pw.println(sb.toString());
			});	
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}