package com.example.binanceparser;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.LogsEventSource;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.FuturesBalanceStateProcessor;
import com.example.binanceparser.processor.SpotBalanceProcessor;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
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

	//TODO ці закоментовані методи - це графік для фючерсів. відрефакторити ці методи на подобі СПОТа.
	// а ще краще - винести в окремий клас для фючерсної візуалізації.

	/*private static BalanceVisualizerConfig configure() {
		final BalanceVisualizerConfig config = new BalanceVisualizerConfig();
		LocalDateTime start = appProperties.getStartTrackDate();
		LocalDateTime finish = appProperties.getEndTrackDate();
		String inputPath = appProperties.getInputFilePath();
		String outputPath = appProperties.getOutputPath();
		config.setStartTrackDate(start);
		config.setFinishTrackDate(finish);
		config.setInputFilepath(inputPath);
		config.setOutputDir(outputPath);
		// config.setAssetsToTrack(List.of(USDT, BUSD, BTC, ETH, AXS));
		config.setAssetsToTrack(appProperties.getAssetsToTrack());
		config.setConvertToUSD(true);
		return config;
	}
	
	private static void addSubject(BalanceVisualizerConfig config, String subject, String prefix) {
		final String resolvedSubjectName = prefix.equals("") ? subject : prefix + "_" + subject;
		final List<String> list = config.getSubject();
		if (list == null) {
			config.setSubject(new ArrayList<>());
		}
		config.getSubject().add(resolvedSubjectName);
	}
	
	public void futuresStateChangeFromLogs() throws IOException {
		final BalanceVisualizerConfig config = configure();
		final String person = appProperties.getTrackedPerson();
		String prefix = appProperties.getFuturesAccountPrefix();
		addSubject(config, person, prefix);
		final EventSource<AbstractEvent> eventSource = getEventSource(config);
		FuturesBalanceStateProcessor processor = new FuturesBalanceStateProcessor(eventSource, config);
		final BalanceReport report = processor.process();
		System.out.println("Report....");
		System.out.println(report.toPrettyString());
	}
	
	private EventSource<AbstractEvent> getEventSource(BalanceVisualizerConfig config) {
    	AppProperties.DatasourceType eventSourceType = appProperties.getDataSourceType();
    	String person = appProperties.getTrackedPerson();
    	final File logsDir = new File(config.getInputFilepath());
    	EventSource<AbstractEvent> eventSource;
    	switch (eventSourceType){
			case CSV:
				eventSource = new CSVEventSource(logsDir, person);
				break;
			case LOGS:
				eventSource = new LogsEventSource(logsDir, filters(config));
				break;
			default:
				throw new RuntimeException("unknown event source type specified");
		}
    	return eventSource;
    }
	*/
}