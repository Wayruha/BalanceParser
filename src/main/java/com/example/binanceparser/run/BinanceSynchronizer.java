package com.example.binanceparser.run;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.synchronizers.CSVEventSynchronizer;
import com.example.binanceparser.datasource.synchronizers.DataSynchronizer;
import com.example.binanceparser.domain.events.AbstractEvent;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
public class BinanceSynchronizer {
    public static void main(String[] args) throws IOException {
        AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/synchronizer.properties");
        DataSource<AbstractEvent> source = null;
        File data = new File(appProperties.getInputFilePath());
        DataSynchronizer<AbstractEvent> synchronizer = new CSVEventSynchronizer(appProperties.getTrackedPersons(), source, data);
        synchronizer.synchronize();
    }
}
