package com.example.binanceparser.run;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.SpotBalanceProcessor;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.processor.NamePostProcessor;

import java.io.IOException;
import java.util.*;

public class SpotBalanceStateVisualizer extends BalanceStateVisualizer {
	private AppProperties appProperties;

	public SpotBalanceStateVisualizer(AppProperties properties) {
		this.appProperties = properties;
	}

	public BalanceReport spotBalanceVisualisation(String user) throws IOException {
		final BalanceVisualizerConfig config = ConfigUtil.loadVisualizerConfig(appProperties);
		config.setSubject(List.of(user));
		final EventSource<AbstractEvent> eventSource = getEventSource(appProperties.getDataSourceType(), config);
		final SpotBalanceProcessor processor = new SpotBalanceProcessor(eventSource, config);
		processor.registerPostProcessor(new NamePostProcessor(config));
		final BalanceReport testReport = processor.process();
		return testReport;
	}
}