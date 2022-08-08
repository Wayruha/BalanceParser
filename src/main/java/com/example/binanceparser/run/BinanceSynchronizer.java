package com.example.binanceparser.run;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.synchronizers.CSVIncomeHistorySynchronizer;
import com.example.binanceparser.datasource.synchronizers.DataSynchronizer;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
public class BinanceSynchronizer {
    public static void main(String[] args) throws IOException {
        AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/synchronizer.properties");
        File data = new File(appProperties.getInputFilePath());
        DataSource<IncomeHistoryItem> source = null;
        DataSynchronizer<IncomeHistoryItem> synchronizer = new CSVIncomeHistorySynchronizer(appProperties.getTrackedPersons(), source, data);
        synchronizer.synchronize();
    }
}
