package com.example.binanceparser;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.parse;

@Data
public class AppProperties {
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private List<String> trackedPersons;
	private String futuresAccountPrefix;
	private String spotAccountPrefix;
	private LocalDateTime startTrackDate;
	private LocalDateTime endTrackDate;
	private String inputFilePath;
	private String incomeInputFilePath;
	private String outputPath;
	private List<String> assetsToTrack;
	private DatasourceType dataSourceType;
	private HistoryItemSourceType historyItemSourceType;
	private Level loggerLevel;

	public AppProperties(Properties props) {
		this.trackedPersons = personsTotrack(props);
		this.futuresAccountPrefix = props.getProperty("config.futures_prefix");
		this.spotAccountPrefix = props.getProperty("config.spot_prefix");
		this.startTrackDate = parse(props.getProperty("config.start_track_date"), formatter);
		this.endTrackDate = parse(props.getProperty("config.finish_track_date"), formatter);
		this.inputFilePath = props.getProperty("config.file_input_path");
		this.incomeInputFilePath = props.getProperty("config.income.file_input_path");
		this.outputPath = props.getProperty("config.file_output_path");
		this.assetsToTrack = assetsToTrack(props);
		this.dataSourceType = DatasourceType.forName(props.getProperty("config.event_source_type"));
		this.historyItemSourceType = HistoryItemSourceType.forName(props.getProperty("config.income.source_type"));
		this.loggerLevel = Level.parse(props.getProperty("config.logger_level"));
	}

	private static List<String> assetsToTrack(Properties props) {
		List<String> assetsToTrack = Arrays.asList(props.getProperty("config.assets_to_track").split(","));
		assetsToTrack = assetsToTrack.stream().map(String::trim).filter((asset) -> !asset.equals(""))
				.collect(Collectors.toList());
		return assetsToTrack;
	}

	private static List<String> personsTotrack(Properties props) {
		List<String> personsTotrack = Arrays.asList(props.getProperty("config.persons").split(","));
		personsTotrack = personsTotrack.stream()
				.map(String::trim)
				.filter(StringUtils::isNotEmpty)
				.collect(Collectors.toList());
		return personsTotrack;
	}

	public enum DatasourceType {
		LOGS("logs"), CSV("csv");

		private final String name;

		DatasourceType(String name) {
			this.name = name;
		}

		public static DatasourceType forName(String str) {
			return Arrays.stream(values()).filter(t -> t.name.equals(str)).findFirst().orElseThrow();
		}
	}

	public enum HistoryItemSourceType {
		LOGS("logs"), BINANCE("binance");

		private final String name;

		HistoryItemSourceType(String name) {
			this.name = name;
		}

		public static HistoryItemSourceType forName(String str) {
			return Arrays.stream(values()).filter(t -> t.name.equals(str)).findFirst().orElseThrow();
		}
	}
}