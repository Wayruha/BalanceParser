package com.example.binanceparser;

import java.io.IOException;
import java.util.List;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.FuturesBalanceStateProcessor;
import com.example.binanceparser.report.BalanceReport;

public class FuturesBalanceStateVisualizer extends BalanceStateVisualizer {
	private AppProperties appProperties;
	
	public FuturesBalanceStateVisualizer(AppProperties properties) {
		this.appProperties = properties;
	}

	public static void main(String[] args) throws IOException {
		final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/application.properties");
		FuturesBalanceStateVisualizer visualizer = new FuturesBalanceStateVisualizer(appProperties);
		final String trackedPerson = appProperties.getTrackedPersons().get(0);
		final BalanceReport report = visualizer.futuresBalanceVisualisation(trackedPerson, null);
		System.out.println("Futures report for " + trackedPerson + ":");
		System.out.println(report.toPrettyString());
	}
	
	public BalanceReport futuresBalanceVisualisation(String user, BalanceVisualizerConfig _config) throws IOException {
		final BalanceVisualizerConfig config = ConfigUtil.loadVisualizerConfig(appProperties);
		config.setSubject(List.of(user));
		final EventSource<AbstractEvent> eventSource = getEventSource(appProperties.getDataSourceType(), config);
		final FuturesBalanceStateProcessor processor = new FuturesBalanceStateProcessor(eventSource, config);
		final BalanceReport testReport = processor.process();
		return testReport;
	}
}
