package com.example.binanceparser.run;

import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.datasource.CSVEventSource;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.FuturesOrderStatusFilter;
import com.example.binanceparser.datasource.filters.ReduceOnlyFilter;
import com.example.binanceparser.processor.StatsProcessor;
import com.example.binanceparser.report.StatsReport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class StatsVisualizer {
    private static final Logger log = Logger.getLogger(StatsVisualizer.class.getName());
    private AppProperties appProperties;

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
        File logsDir = new File(config.getInputFilepath());
        CSVEventSource eventSource = new CSVEventSource(logsDir, config.getSubject());//TODO other event sources
        StatsProcessor processor = new StatsProcessor(config, eventSource.getData());
        StatsReport report = processor.process();
        System.out.println("Report....");
        System.out.println(report.toPrettyString());
        return report;
    }

    private List<Filter> filters() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new FuturesOrderStatusFilter(OrderStatus.FILLED));
        filters.add(new ReduceOnlyFilter(true));
        return filters;
    }
}
