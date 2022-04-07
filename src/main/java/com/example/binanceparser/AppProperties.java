package com.example.binanceparser;

import com.binance.api.client.FuturesIncomeType;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.parse;
import static java.util.Optional.ofNullable;

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
	private String namesFilePath;
	private List<String> assetsToTrack;
	private DatasourceType dataSourceType;
	private HistoryItemSourceType historyItemSourceType;
	private List<FuturesIncomeType> incomeTypes;

	public AppProperties(Properties props) {
		this.trackedPersons = personsToTrack(props);
		this.futuresAccountPrefix = props.getProperty("config.futures_prefix");
		this.spotAccountPrefix = props.getProperty("config.spot_prefix");
		this.startTrackDate = parse(props.getProperty("config.start_track_date"), formatter);
		this.endTrackDate = parse(props.getProperty("config.finish_track_date"), formatter);
		this.inputFilePath = props.getProperty("config.file_input_path");
		this.incomeInputFilePath = props.getProperty("config.income.keys");
		this.outputPath = props.getProperty("config.file_output_path");
		this.namesFilePath = props.getProperty("config.names_file_path");
		this.assetsToTrack = assetsToTrack(props);
		this.dataSourceType = ofNullable(props.getProperty("config.event_source_type")).map(DatasourceType::forName).orElse(null);
		this.historyItemSourceType = ofNullable(props.getProperty("config.income.source_type")).map(HistoryItemSourceType::forName).orElse(null);
		this.incomeTypes = parseIncomeTypes(props.getProperty("config.income.income_types"));
	}

	private List<FuturesIncomeType> parseIncomeTypes(String property) {
		if(property == null) return Collections.emptyList();
		return Arrays.stream(property.split(","))
				.map(type -> FuturesIncomeType.valueOf(type.trim())).collect(Collectors.toList());
	}

	private static List<String> assetsToTrack(Properties props) {
		if(props.getProperty("config.assets_to_track") == null) return null;
		List<String> assetsToTrack = Arrays.asList(props.getProperty("config.assets_to_track").split(","));
		assetsToTrack = assetsToTrack.stream().map(String::trim).filter((asset) -> !asset.equals(""))
				.collect(Collectors.toList());
		return assetsToTrack;
	}

	private static List<String> personsToTrack(Properties props) {
		List<String> personsToTrack = Arrays.asList(props.getProperty("config.persons").split(","));
		personsToTrack = personsToTrack.stream()
				.map(String::trim)
				.filter(StringUtils::isNotEmpty)
				.collect(Collectors.toList());
		return personsToTrack;
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