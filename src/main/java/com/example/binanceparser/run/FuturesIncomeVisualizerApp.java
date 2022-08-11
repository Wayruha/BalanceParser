package com.example.binanceparser.run;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.models.UserApiData;
import com.example.binanceparser.datasource.sources.BinanceIncomeDataSource;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.sources.JsonIncomeSource;
import com.example.binanceparser.processor.IncomeProcessor;
import com.example.binanceparser.report.BalanceReport;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.List.of;

public class FuturesIncomeVisualizerApp {
    private static final Logger log = Logger.getLogger(FuturesIncomeVisualizerApp.class.getName());
    private AppProperties appProperties;
    @Getter
    private Map<String, UserApiData> userApiKeys;

    public static void main(String[] args) throws IOException {
        final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/futures-income.properties");
        FuturesIncomeVisualizerApp visualizer = new FuturesIncomeVisualizerApp(appProperties);
        final String trackedPerson = appProperties.getTrackedPersons().get(0);
        final BalanceReport report = visualizer.futuresIncomeVisualisation(trackedPerson, null);
        log.info("FuturesIncome report for " + trackedPerson + ":");
        log.info(report.toPrettyString());
    }

    public FuturesIncomeVisualizerApp(AppProperties appProperties) {
        this.appProperties = appProperties;
        final List<UserApiData> userData = Helper.getUserData(appProperties);
        this.userApiKeys = userData.stream().collect(Collectors.toMap(UserApiData::getUserId, Function.identity()));
    }

    public BalanceReport futuresIncomeVisualisation(String user, IncomeConfig _config) {
        final IncomeConfig config = ConfigUtil.loadIncomeConfig(appProperties);
        final UserApiData userData = userApiKeys.get(user);
        DataSource<IncomeHistoryItem> apiClientSource = getEventSource(userData, config);
        IncomeProcessor processor = new IncomeProcessor(apiClientSource, config);
        return processor.process();
    }

    public DataSource<IncomeHistoryItem> getEventSource(UserApiData user, IncomeConfig config) {
        switch (appProperties.getHistoryItemSourceType()) {
            case LOGS:
                return new JsonIncomeSource(new File(config.getInputFilepath()));
            case BINANCE:
                config.setLimit(1000);
                config.setSubjects(of(user.getUserId()));
                config.setIncomeTypes(config.getIncomeTypes());
                String apiKey = user.getApiKey();
                String secretKey = user.getSecretKey();
                return new BinanceIncomeDataSource(apiKey, secretKey, config);
            default:
                throw new UnsupportedOperationException();
        }
    }

//    private List<UserApiData> getUserData(AppProperties appProperties) {
//        try {
//            final File userAPIInput = new File(appProperties.getIncomeInputFilePath());
//            return new CSVDataSource<>(userAPIInput, UserApiData.class).getData();
//        } catch (IllegalStateException e) {
//            throw new RuntimeException(e);
//        }
//    }
}