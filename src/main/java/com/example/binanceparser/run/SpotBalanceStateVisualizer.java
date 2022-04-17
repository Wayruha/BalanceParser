package com.example.binanceparser.run;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.SpotBalanceProcessor;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.processor.NamePostProcessor;
import com.example.binanceparser.report.processor.PostProcessor;
import com.example.binanceparser.report.processor.ReportSerializer;

import java.io.IOException;
import java.util.List;

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
        final DataSource<UserNameRel> nameSource = Helper.getNameSource(appProperties.getDataSourceType(), config);
        final SpotBalanceStateVisualizer visualizer = new SpotBalanceStateVisualizer(appProperties, config, eventSource, nameSource);

        final BalanceReport report = visualizer.spotBalanceVisualisation();
        System.out.println(report.toPrettyString());
    }

    public BalanceReport spotBalanceVisualisation() throws IOException {
        final DataWriter<BalanceReport> reportWriter = Helper.getReportWriter(appProperties.getReportOutputType(), config);
        final SpotBalanceProcessor processor = new SpotBalanceProcessor(eventSource, config);
        final List<PostProcessor<AbstractEvent, BalanceReport>> postProcessors = List.of(
                new NamePostProcessor(nameSource, config),
                new ReportSerializer<>(reportWriter)
        );
        processor.registerPostProcessor(postProcessors);
        final BalanceReport testReport = processor.process();
        return testReport;
    }
}