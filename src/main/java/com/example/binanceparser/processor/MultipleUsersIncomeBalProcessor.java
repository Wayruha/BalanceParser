package com.example.binanceparser.processor;

import com.binance.api.client.domain.account.request.IncomeHistoryItem;
import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.models.UserApiData;
import com.example.binanceparser.datasource.sources.*;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.AggregatedBalanceReport;
import com.example.binanceparser.report.BalanceReport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.List.of;

public class MultipleUsersIncomeBalProcessor extends Processor<IncomeHistoryItem, AggregatedBalanceReport> {

    private final IncomeConfig config;
    private final AppProperties.HistoryItemSourceType type;
    private final Map<String, UserApiData> userApiKeys;

    public MultipleUsersIncomeBalProcessor(IncomeConfig config, AppProperties.HistoryItemSourceType type) {
        super(null);
        this.config = config;
        this.type = type;
        this.userApiKeys = getUserData(config.getInputFilepath()).stream().collect(Collectors.toMap(UserApiData::getUserId, Function.identity()));
    }

    @Override
    public AggregatedBalanceReport process() {
        final List<BalanceReport> reports = new ArrayList<>();

        List<String> users = config.getSubjects();
        if (users.isEmpty()) {
            final DataSource<AbstractEvent> redundantEventSource = new CSVEventSource(new File(config.getInputFilepath()), Collections.emptyList());
            users = extractAllUsersIds(redundantEventSource);
        }

        users.stream().forEach(user -> {
            final UserApiData userData = userApiKeys.get(user);
            List<IncomeHistoryItem> apiClientSource = getEventSource(userData).getData();
            AggregatedBalanceReport rep = process(apiClientSource);
            reports.addAll(rep.getReports());
//            IncomeProcessor processor = new IncomeProcessor(apiClientSource, config);
//            reports.add(processor.process());
            postProcessors.forEach(pp -> pp.processReport(rep, apiClientSource));
        });
        return new AggregatedBalanceReport(reports);
    }

    @Override
    protected AggregatedBalanceReport process(List<IncomeHistoryItem> data) {
        final List<BalanceReport> reports = new ArrayList<>();
        final var datasource = new InMemoryHistoryItemSource(data.stream());
        IncomeProcessor processor = new IncomeProcessor(datasource, config);
        reports.add(processor.process());
        return new AggregatedBalanceReport(reports);
    }

    public DataSource<IncomeHistoryItem> getEventSource(UserApiData user) {
        switch (type) {
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

    private List<UserApiData> getUserData(String inputPath) {
        try {
            final File userAPIInput = new File(inputPath);
            return new CSVDataSource<>(userAPIInput, UserApiData.class).getData();
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> extractAllUsersIds(DataSource<AbstractEvent> redundantEventSource) {
        return redundantEventSource.getData().stream()
                .map(AbstractEvent::getSource)
                .distinct()
                .collect(Collectors.toList());
    }
}
