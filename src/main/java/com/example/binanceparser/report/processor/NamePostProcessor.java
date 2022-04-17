package com.example.binanceparser.report.processor;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.BalanceReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

public class NamePostProcessor extends PostProcessor<AbstractEvent, BalanceReport> {
    private static final String DEFAULT_NAME = "unknown";
    private final Map<String, String> usersNames;
    private final DataSource<UserNameRel> dataSource;

    public NamePostProcessor(DataSource<UserNameRel> dataSource, BalanceVisualizerConfig config) {
        super(config);
        this.dataSource = dataSource;
        this.usersNames = getUsersNames();
    }

    @Override
    public void processReport(BalanceReport report, List<AbstractEvent> events) {
        String name = ofNullable(usersNames.get(report.getUser())).orElse(DEFAULT_NAME);
        report.setName(name);
    }

    private Map<String, String> getUsersNames() {
        Map<String, String> map = new HashMap<>();
        dataSource.getData().stream().forEach((model) -> map.put(model.getUser(), model.getName()));
        return map;
    }
}
