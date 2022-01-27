package com.example.binanceparser.config;

import com.example.binanceparser.AppProperties;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;

public class ConfigUtil {

    public static AppProperties loadAppProperties(String propertyFile) throws IOException {
        final Properties prop = new Properties();
        prop.load(new FileReader(propertyFile));
        AppProperties appProperties = new AppProperties(prop);
        return appProperties;
    }

    public static BalanceVisualizerConfig loadConfig(AppProperties appProperties) {
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
}
