package com.example.binanceparser;

import java.io.IOException;
import java.util.List;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.FuturesBalanceStateProcessor;
import com.example.binanceparser.report.BalanceReport;

public class FuturesBalanceStateVisualizer extends BalanceStateVisualizer {
	private AppProperties appProperties;
	
	public FuturesBalanceStateVisualizer(AppProperties properties) {
		this.appProperties = properties;
	}
	
	public BalanceReport futuresStateChangeFromLogs(String user, BalanceVisualizerConfig config) throws IOException {
		config.setSubject(List.of(user));
		final EventSource<AbstractEvent> eventSource = getEventSource(appProperties.getDataSourceType(), config);
		final FuturesBalanceStateProcessor processor = new FuturesBalanceStateProcessor(eventSource, config);
		final BalanceReport testReport = processor.process();
		return testReport;
	}
}
