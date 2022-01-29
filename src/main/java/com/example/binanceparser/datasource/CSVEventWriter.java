package com.example.binanceparser.datasource;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.example.binanceparser.domain.events.AbstractEvent;

public class CSVEventWriter implements EventWriter<AbstractEvent> {
	private File outputDir;
	private String personId;

	public CSVEventWriter(File outputDir, String personId) {
		this.outputDir = outputDir;
		this.personId = personId;
	}

	@Override
	public void writeEvents(List<AbstractEvent> events) {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		List<CSVModel> models = events.stream().map((event) -> {
			CSVModel model = null;
			try {
				model = new CSVModel(personId, event.getEventType().toString(),
						event.getDateTime().format(dateFormat).toString(),
						new ObjectMapper().writer().writeValueAsString(event));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return model;
		}).collect(Collectors.toList());

		try {
			CsvSchema schema = CsvSchema.builder().addColumn("customer_id").addColumn("event_type").addColumn("event_ts").addColumn("json")
					.build().withHeader();
			CsvMapper mapper = new CsvMapper();
			mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
			// here is some problem
			mapper.writerFor(CSVModel.class).with(schema).writeValues(outputDir).writeAll(models);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}