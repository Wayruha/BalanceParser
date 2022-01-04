package com.example.binanceparser;

import static com.example.binanceparser.Constants.*;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.LogsEventSource;
import com.example.binanceparser.datasource.filters.EventTypeFilter;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.processor.FuturesBalanceStateProcessor;
import com.example.binanceparser.processor.SpotBalanceProcessor;
import com.example.binanceparser.processor.TestSpotBalanceProcessor;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BalanceStateVisualizer {
	public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) throws IOException {
		BalanceStateVisualizer app = new BalanceStateVisualizer();
		final String person = "a.nefedov";
		app.futuresStateChangeFromLogs(person);
		//app.spotStateChangeFromLogs(person);
	}

	public void futuresStateChangeFromLogs(String person) throws IOException {
		final BalanceVisualizerConfig config = configure();
		addSubject(config, person, "FUTURES");

		final File logsDir = new File(config.getInputFilepath());
		final LogsEventSource logsEventSource = new LogsEventSource(logsDir, filters(config));
		FuturesBalanceStateProcessor processor = new FuturesBalanceStateProcessor(logsEventSource, config);
		final BalanceReport report = processor.process();
		System.out.println("Report....");
		System.out.println(report.toPrettyString());
	}

	public void spotStateChangeFromLogs(String person) throws IOException {
		final BalanceVisualizerConfig config = configure();
		addSubject(config, person, "SPOT");

		final File logsDir = new File(config.getInputFilepath());
		final LogsEventSource logsEventSource = new LogsEventSource(logsDir, filters(config));
//		SpotBalanceProcessor processor = new SpotBalanceProcessor(logsEventSource, config);
		TestSpotBalanceProcessor testProcessor = new TestSpotBalanceProcessor(logsEventSource, config);
//		final BalanceReport report = processor.process();
		final BalanceReport testReport = testProcessor.process();
//		System.out.println("Report....");
//		System.out.println(report.toPrettyString());
		System.out.println("Test report....");
		System.out.println(testReport.toPrettyString());
	}

	private static BalanceVisualizerConfig configure() {
		final BalanceVisualizerConfig config = new BalanceVisualizerConfig();
		LocalDateTime start = LocalDateTime.parse("2021-08-16 00:00:00", dateFormat);
		LocalDateTime finish = LocalDateTime.parse("2021-12-30 00:00:00", dateFormat);
		config.setStartTrackDate(start);
		config.setFinishTrackDate(finish);
		config.setInputFilepath("C:/Users/Sanya/Desktop/ParserOutput/events");
		config.setOutputDir("C:/Users/Sanya/Desktop/ParserOutput");
		//config.setAssetsToTrack(List.of(USDT, BUSD, BTC, ETH, AXS));
		config.setAssetsToTrack(Collections.emptyList());
		config.setConvertToUSD(true);
		return config;
	}

	private static void addSubject(BalanceVisualizerConfig config, String subject, String prefix) {
		final String resolvedSubjectName = prefix + "_" + subject;
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
}