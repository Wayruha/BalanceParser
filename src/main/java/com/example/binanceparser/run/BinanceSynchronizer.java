package com.example.binanceparser.run;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.models.UserApiData;
import com.example.binanceparser.datasource.sources.BinanceIncomeDataSource;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.synchronizers.CSVIncomeHistorySynchronizer;
import com.example.binanceparser.datasource.synchronizers.DataSynchronizer;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class BinanceSynchronizer {
    private static Map<String, UserApiData> userApiKeys;

    public static void main(String[] args) throws IOException {
        AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/synchronizer.properties");
        userApiKeys = Helper.getUserData(appProperties).stream().collect(Collectors.toMap(UserApiData::getUserId, Function.identity()));;
        File data = new File(appProperties.getInputFilePath());
        IncomeConfig config = ConfigUtil.loadIncomeConfig(appProperties);
        String person = appProperties.getTrackedPersons().get(0);
        DataSource<IncomeHistoryItem> source = new BinanceIncomeDataSource(userApiKeys.get(person).getApiKey(), userApiKeys.get(person).getSecretKey(), config);
        DataSynchronizer<IncomeHistoryItem> synchronizer = new CSVIncomeHistorySynchronizer(source, data);
        synchronizer.synchronize();
    }
}
