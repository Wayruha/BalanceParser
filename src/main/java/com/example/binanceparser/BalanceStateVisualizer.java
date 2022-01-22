package com.example.binanceparser;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.LogsEventSource;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.domain.BalanceState;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.FuturesBalanceStateProcessor;
import com.example.binanceparser.processor.SpotBalanceProcessor;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import static com.example.binanceparser.Constants.*;

public class BalanceStateVisualizer {

	private static Properties appProperties;

	public static void main(String[] args) throws IOException {
		appProperties = new Properties();
		appProperties.load(new FileReader("src/main/resources/application.properties"));
		BalanceStateVisualizer app = new BalanceStateVisualizer();
		final String person = appProperties.getProperty("config.person");
		// app.futuresStateChangeFromLogs(person);
		app.spotStateChangeFromLogs(person);
	}

	public void futuresStateChangeFromLogs(String person) throws IOException {
		final BalanceVisualizerConfig config = configure();
		String prefix = appProperties.getProperty("config.futures_prefix");
		addSubject(config, person, prefix);
		final EventSource<AbstractEvent> eventSource = getEventSource(config);
		FuturesBalanceStateProcessor processor = new FuturesBalanceStateProcessor(eventSource, config);
		final BalanceReport report = processor.process();
		System.out.println("Report....");
		System.out.println(report.toPrettyString());
	}

	public void spotStateChangeFromLogs(String person) throws IOException {
		final BalanceVisualizerConfig config = configure();
		String prefix = appProperties.getProperty("config.spot_prefix");
		addSubject(config, person, prefix);
		final EventSource<AbstractEvent> eventSource = getEventSource(config);
		SpotBalanceProcessor testProcessor = new SpotBalanceProcessor(eventSource, config);
		final BalanceReport testReport = testProcessor.process();
		System.out.println("Test report....");
		System.out.println(testReport.toPrettyString());
	}

	private static BalanceVisualizerConfig configure() {
		final BalanceVisualizerConfig config = new BalanceVisualizerConfig();
		String startTrackDate = appProperties.getProperty("config.start_track_date");
		String finishTrackDate = appProperties.getProperty("config.finish_track_date");
		String inputPath = appProperties.getProperty("config.file_input_path");
		String outputPath = appProperties.getProperty("config.file_output_path");
		LocalDateTime start = LocalDateTime.parse(startTrackDate, DATE_FORMAT);
		LocalDateTime finish = LocalDateTime.parse(finishTrackDate, DATE_FORMAT);
		config.setStartTrackDate(start);
		config.setFinishTrackDate(finish);
		config.setInputFilepath(inputPath);
		config.setOutputDir(outputPath);
		// config.setAssetsToTrack(List.of(USDT, BUSD, BTC, ETH, AXS));
		config.setAssetsToTrack(assetsToTrack());
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

	private static Set<Filter> filters(BalanceVisualizerConfig config) {
		final Set<Filter> filters = new HashSet<>();
		if (config.getSubject() != null) {
			filters.add(new SourceFilter(config.getSubject()));
		}

		if (config.getEventType() != null) {
			filters.add(new EventTypeFilter(config.getEventType()));
		}
		return filters;
	}

	private EventSource<AbstractEvent> getEventSource(BalanceVisualizerConfig config) {
		String eventSourceType = appProperties.getProperty("config.event_source_type");
		String person = appProperties.getProperty("config.person");
		final File logsDir = new File(config.getInputFilepath());
		EventSource<AbstractEvent> eventSource;
		if (eventSourceType.equalsIgnoreCase("logs")) {
			eventSource = new LogsEventSource(logsDir, filters(config));
		} else if (eventSourceType.equalsIgnoreCase("csv")) {
			eventSource = new CSVEventSource(logsDir, person);
		} else {
			throw new RuntimeException("unknown event source type specified");
		}
		return eventSource;
	}

	private static List<String> assetsToTrack() {
		List<String> assetsToTrack = Arrays.asList(appProperties.getProperty("config.assets_to_track").split(","));
		assetsToTrack = assetsToTrack.stream().map(String::trim).collect(Collectors.toList());
		return assetsToTrack;
	}
}