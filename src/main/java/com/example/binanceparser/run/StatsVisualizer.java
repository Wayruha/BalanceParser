package com.example.binanceparser.run;

import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.datasource.EventSource;
import com.example.binanceparser.datasource.LogsEventSource;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.FuturesOrderStatusFilter;
import com.example.binanceparser.datasource.filters.ReduceOnlyFilter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.StatsProcessor;
import com.example.binanceparser.report.StatsReport;

import java.io.File;
import java.io.IOException;
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

    public static void main(String[] args) throws IOException {
        final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/stats-visualisation.properties");
        StatsVisualizer visualizer = new StatsVisualizer(appProperties);
        final StatsReport report = visualizer.visualizeStats(appProperties.getTrackedPersons());
        log.info(report.toPrettyString());
    }

    public StatsReport visualizeStats(List<String> users) throws IOException {
        StatsVisualizerConfig config = ConfigUtil.loadStatsConfig(appProperties);
        config.setFilters(filters());
        config.setSubject(users);
        EventSource<AbstractEvent> eventSource = getEventSource(appProperties.getDataSourceType(), config);
        StatsProcessor processor = new StatsProcessor(config, eventSource.getData());
        StatsReport report = processor.process();
        System.out.println("Report....");
        System.out.println(report.toPrettyString());
        return report;
    }

    private static Set<Filter> filters() {
        Set<Filter> filters = new HashSet<>();
        filters.add(new FuturesOrderStatusFilter(OrderStatus.FILLED));
        filters.add(new ReduceOnlyFilter(true));
        return filters;
    }

    public static EventSource<AbstractEvent> getEventSource(AppProperties.DatasourceType datasourceType, StatsVisualizerConfig config) {
        final File logsDir = new File(config.getInputFilepath());
        EventSource<AbstractEvent> eventSource;
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
