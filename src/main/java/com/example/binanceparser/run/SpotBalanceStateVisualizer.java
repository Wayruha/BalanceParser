package com.example.binanceparser.run;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.SpotBalanceProcessor;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.processor.BalanceReportPostProcessor;
import com.example.binanceparser.report.processor.NamePostProcessor;
import com.example.binanceparser.report.processor.PostProcessor;

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
		final DataSource<AbstractEvent> eventSource = getEventSource(appProperties.getDataSourceType(), config);
		final DataSource<UserNameRel> nameSource = getNameSource(appProperties.getDataSourceType(), config);
		final SpotBalanceProcessor processor = new SpotBalanceProcessor(eventSource, config);
		final List<PostProcessor<AbstractEvent>> postProcessors = List.of(
				new NamePostProcessor(nameSource, config),
				new BalanceReportPostProcessor(getReportWriter(appProperties.getReportOutputType(), config))
		);
		processor.registerPostProcessor(postProcessors);
		final BalanceReport testReport = processor.process();
		return testReport;
	}
}