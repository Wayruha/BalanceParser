package com.example.binanceparser;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.parse;

@Data
public class AppProperties {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String trackedPerson;
    private String futuresAccountPrefix;
    private String spotAccountPrefix;
    private LocalDateTime startTrackDate;
    private LocalDateTime endTrackDate;
    private String inputFilePath;
    private String outputPath;
    private List<String> assetsToTrack;
    private DatasourceType dataSourceType;

    public AppProperties(Properties props) {
        this.trackedPerson = props.getProperty("config.person");
        this.futuresAccountPrefix = props.getProperty("config.futures_prefix");
        this.spotAccountPrefix = props.getProperty("config.spot_prefix");
        this.startTrackDate = parse(props.getProperty("config.start_track_date"), formatter);
        this.endTrackDate = parse(props.getProperty("config.finish_track_date"), formatter);
        this.inputFilePath = props.getProperty("config.file_input_path");
        this.outputPath = props.getProperty("config.file_output_path");
        this.assetsToTrack = assetsToTrack(props);
        this.dataSourceType = DatasourceType.forName(props.getProperty("config.event_source_type"));
    }
    
    private static List<String> assetsToTrack(Properties props) {
		List<String> assetsToTrack = Arrays.asList(props.getProperty("config.assets_to_track").split(","));
		assetsToTrack = assetsToTrack.stream().map(String::trim).collect(Collectors.toList());
		return assetsToTrack;
	}

    public enum DatasourceType {
        LOGS("logs"),
        CSV("csv");

        private final String name;

        DatasourceType(String name) {
            this.name = name;
        }

        public static DatasourceType forName(String str) {
            return Arrays.stream(values())
                    .filter(t -> t.name.equals(str))
                    .findFirst().orElseThrow();
        }
    }
}
