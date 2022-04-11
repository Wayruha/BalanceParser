package com.example.binanceparser.statistics;

import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.Config;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.datasource.sources.CSVEventSource;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.sources.EventSource;
import com.example.binanceparser.datasource.sources.LogsEventSource;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.FuturesOrderStatusFilter;
import com.example.binanceparser.datasource.filters.ReduceOnlyFilter;
import com.example.binanceparser.domain.events.AbstractEvent;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class StatsVisualizer {
    private static final Logger log = Logger.getLogger(StatsVisualizer.class.getName());
    private final AppProperties appProperties;

    public StatsVisualizer(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public static void main(String[] args) throws Exception {
        final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/stats-visualisation.properties");
        final StatsVisualizer visualizer = new StatsVisualizer(appProperties);
        List<String> users = appProperties.getTrackedPersons();
        if (users.isEmpty()) {
            users = new CSVEventSource(new File(appProperties.getInputFilePath()), appProperties.getTrackedPersons()).getUserIds();
        }
        final List<StatsReport> reports = visualizer.calculateStatistics(users);
        reports.forEach(report -> {
            log.info("Report: " + report.getType());
            log.info(report.toString());
        });
    }

    public List<StatsReport> calculateStatistics(List<String> users) {
        final StatsVisualizerConfig config = ConfigUtil.loadStatsConfig(appProperties);
        config.setFilters(filters());
        config.setSubject(users);
        final DataSource<AbstractEvent> eventSource = getEventSource(appProperties.getDataSourceType(), config);
        final StatsProcessor processor = new StatsProcessor(config, eventSource);
        final List<StatsReport> reports = processor.process();
        return reports;
    }

    private static Set<Filter> filters() {
        final Set<Filter> filters = new HashSet<>();
        filters.add(new FuturesOrderStatusFilter(List.of(OrderStatus.FILLED, OrderStatus.PARTIALLY_FILLED)));
        filters.add(new ReduceOnlyFilter(true));
        return filters;
    }

    public static DataSource<AbstractEvent> getEventSource(AppProperties.DatasourceType datasourceType, Config config) {
        final File logsDir = new File(config.getInputFilepath());
        DataSource<AbstractEvent> eventSource;
        switch (datasourceType) {
            case CSV:
                eventSource = new CSVEventSource(logsDir, config.getSubject());
                break;
            case LOGS:
                eventSource = new LogsEventSource(logsDir, filters());
                break;
            default:
                throw new RuntimeException("unknown event source type specified");
        }
        return eventSource;
    }
}
