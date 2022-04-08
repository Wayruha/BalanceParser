package com.example.binanceparser.run;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.FuturesBalanceProcessor;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.processor.NamePostProcessor;
import com.example.binanceparser.report.processor.PostProcessor;
import com.example.binanceparser.report.processor.TradeCountPostProcessor;

public class FuturesBalanceStateVisualizer extends BalanceStateVisualizer {
	private static final Logger log = Logger.getLogger(FuturesBalanceStateVisualizer.class.getName());

	private AppProperties appProperties;
	
	public FuturesBalanceStateVisualizer(AppProperties properties) {
		this.appProperties = properties;
	}

	public static void main(String[] args) throws IOException {
		final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/futures-balance.properties");
		FuturesBalanceStateVisualizer visualizer = new FuturesBalanceStateVisualizer(appProperties);
		final String trackedPerson = appProperties.getTrackedPersons().get(0);
		final BalanceReport report = visualizer.futuresBalanceVisualisation(trackedPerson);
		System.out.println(report.toPrettyString());
	}
	
	public BalanceReport futuresBalanceVisualisation(String user) throws IOException {
		final BalanceVisualizerConfig config = ConfigUtil.loadVisualizerConfig(appProperties);
		config.setSubject(List.of(user));
		final EventSource<AbstractEvent> eventSource = getEventSource(appProperties.getDataSourceType(), config);
		final List<PostProcessor<AbstractEvent>> postProcessors = List.of(new TradeCountPostProcessor(), new NamePostProcessor(config));
		final FuturesBalanceProcessor processor = new FuturesBalanceProcessor(eventSource, config);
		processor.registerPostProcessor(postProcessors);
		final BalanceReport testReport = processor.process();
		return testReport;
	}
}
