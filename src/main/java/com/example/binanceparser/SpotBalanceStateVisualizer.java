package com.example.binanceparser;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.SpotBalanceProcessor;
import com.example.binanceparser.report.BalanceReport;
import java.io.IOException;
import java.util.*;

public class SpotBalanceStateVisualizer extends BalanceStateVisualizer {
	private AppProperties appProperties;

	public SpotBalanceStateVisualizer(AppProperties properties) {
		this.appProperties = properties;
	}

	public BalanceReport spotStateChangeFromLogs(String user, BalanceVisualizerConfig config) throws IOException {
		config.setSubject(List.of(user));
		final EventSource<AbstractEvent> eventSource = getEventSource(appProperties.getDataSourceType(), config);
		final SpotBalanceProcessor processor = new SpotBalanceProcessor(eventSource, config);
		final BalanceReport testReport = processor.process();
		return testReport;
	}
}