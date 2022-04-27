package com.example.binanceparser.run;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.MultipleUsersSpotBalProcessor;
import com.example.binanceparser.report.AggregatedBalanceReport;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.postprocessor.AggregatedBalReportSerializer;

import java.io.IOException;

public class SpotBalanceStateVisualizer {
    private final AppProperties appProperties;
    private final BalanceVisualizerConfig config;
    private final DataSource<AbstractEvent> eventSource;
    private final DataSource<UserNameRel> nameSource;

    public SpotBalanceStateVisualizer(AppProperties appProperties, BalanceVisualizerConfig config,
                                      DataSource<AbstractEvent> eventSource, DataSource<UserNameRel> nameSource) {
        this.appProperties = appProperties;
        this.config = config;
        this.eventSource = eventSource;
        this.nameSource = nameSource;
    }

    public static void main(String[] args) throws IOException {
        final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/spot-balance.properties");

        final BalanceVisualizerConfig config = ConfigUtil.loadVisualizerConfig(appProperties);
        final DataSource<AbstractEvent> eventSource = Helper.getEventSource(appProperties.getDataSourceType(), config);
        final DataSource<UserNameRel> nameSource = Helper.getNameSource(appProperties);
        final SpotBalanceStateVisualizer visualizer = new SpotBalanceStateVisualizer(appProperties, config, eventSource, nameSource);

        final AggregatedBalanceReport report = visualizer.spotBalanceVisualisation();
        report.getReports().forEach(r -> System.out.println(r.toPrettyString() + "\n"));

    }

    public AggregatedBalanceReport spotBalanceVisualisation() throws IOException {
        final DataWriter<BalanceReport> reportWriter = Helper.getReportWriter(appProperties);
        final var processor = new MultipleUsersSpotBalProcessor(eventSource, config, nameSource);
        final var reportSerializer = new AggregatedBalReportSerializer(reportWriter);
        processor.registerPostProcessor(reportSerializer);

        final AggregatedBalanceReport testReport = processor.process();
        return testReport;
    }
}