package com.example.binanceparser.config;

import com.example.binanceparser.AppProperties;

import java.io.FileReader;
import java.io.IOException;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Properties;

public class ConfigUtil {

	public static AppProperties loadAppProperties(String propertyFile) throws IOException {
		final Properties prop = new Properties();
		prop.load(new FileReader(propertyFile));
		AppProperties appProperties = new AppProperties(prop);
		return appProperties;
	}

	public static BalanceVisualizerConfig loadVisualizerConfig(AppProperties appProperties) {
		final BalanceVisualizerConfig config = new BalanceVisualizerConfig();
		final LocalDateTime start = appProperties.getStartTrackDate();
		final LocalDateTime finish = appProperties.getEndTrackDate();
		final String inputPath = appProperties.getInputFilePath();
		final String outputPath = appProperties.getOutputPath();
		final String namesFilePath = appProperties.getNamesFilePath();
		final String reportOutputLocation = appProperties.getReportOutputLocation();
		final String reportOutputDir = appProperties.getReportOutputDir();
		config.setStartTrackDate(start);
		config.setFinishTrackDate(finish);
		config.setInputFilepath(inputPath);
		config.setOutputDir(outputPath);
		config.setNamesFilePath(namesFilePath);
		config.setReportOutputLocation(reportOutputLocation);
		config.setReportOutputDir(reportOutputDir);
		config.setAssetsToTrack(appProperties.getAssetsToTrack());
		config.setConvertToUSD(true);
		config.setSubjects(appProperties.getTrackedPersons());
		return config;
	}

	public static IncomeConfig loadIncomeConfig(AppProperties appProperties) {
		IncomeConfig config = new IncomeConfig();
        LocalDateTime start = appProperties.getStartTrackDate();
        LocalDateTime finish = appProperties.getEndTrackDate();
        String outputPath = appProperties.getOutputPath();
        String inputPath = appProperties.getIncomeInputFilePath();
		String namesFilePath = appProperties.getNamesFilePath();
		String reportOutputLocation = appProperties.getReportOutputLocation();
        config.setStartTrackDate(start);
        config.setFinishTrackDate(finish);
        config.setOutputDir(outputPath);
        config.setInputFilepath(inputPath);
		config.setNamesFilePath(namesFilePath);
		config.setReportOutputLocation(reportOutputLocation);
        config.setIncomeTypes(appProperties.getIncomeTypes());
		config.setAssetsToTrack(appProperties.getAssetsToTrack());
		config.setSubjects(appProperties.getTrackedPersons());
		return config;
	}

	public static StatsVisualizerConfig loadStatsConfig(AppProperties appProperties) {
		StatsVisualizerConfig config = new StatsVisualizerConfig();
		LocalDateTime start = appProperties.getStartTrackDate();
		LocalDateTime finish = appProperties.getEndTrackDate();
		String inputPath = appProperties.getInputFilePath();
		String outputPath = appProperties.getOutputPath();
		config.setStartTrackDate(start);
		config.setFinishTrackDate(finish);
		config.setInputFilepath(inputPath);
		config.setOutputDir(outputPath);
		config.setDelayPrecision(appProperties.getDelayPrecision());
		config.setRelativeContext(new MathContext(appProperties.getPercentagePrecision(), appProperties.getRoundingMode()));
		config.setSubjects(appProperties.getTrackedPersons());
		return config;
	}
}