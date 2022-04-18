package com.example.binanceparser.run;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.FuturesBalanceProcessor;
import com.example.binanceparser.processor.MultipleUsersFuturesBalProcessor;
import com.example.binanceparser.report.AggregatedBalanceReport;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.postprocessor.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class FuturesBalanceStateVisualizer {
    private static final Logger log = Logger.getLogger(FuturesBalanceStateVisualizer.class.getName());

    private final AppProperties appProperties;
    final BalanceVisualizerConfig config;
    final DataSource<AbstractEvent> eventSource;
    final DataSource<UserNameRel> nameSource;

    public FuturesBalanceStateVisualizer(AppProperties appProperties, BalanceVisualizerConfig config,
                                         DataSource<AbstractEvent> eventSource, DataSource<UserNameRel> nameSource) {
        this.appProperties = appProperties;
        this.config = config;
        this.eventSource = eventSource;
        this.nameSource = nameSource;
    }

    public static void main(String[] args) throws IOException {
        final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/futures-balance.properties");

        final BalanceVisualizerConfig config = ConfigUtil.loadVisualizerConfig(appProperties);
        final DataSource<AbstractEvent> eventSource = Helper.getEventSource(appProperties.getDataSourceType(), config);
        final DataSource<UserNameRel> nameSource = Helper.getNameSource(appProperties.getDataSourceType(), config);
        final FuturesBalanceStateVisualizer visualizer = new FuturesBalanceStateVisualizer(appProperties, config, eventSource, nameSource);

        final AggregatedBalanceReport reports = visualizer.futuresBalanceVisualisation();
        reports.getReports().forEach(r -> System.out.println(r.toPrettyString() + "\n"));
    }

    public AggregatedBalanceReport futuresBalanceVisualisation() throws IOException {
        final DataWriter<BalanceReport> reportWriter = Helper.getReportWriter(appProperties.getReportOutputType(), config);
        final var reportSerializer = new AggregatedBalReportSerializer(reportWriter);

        final var processor = new MultipleUsersFuturesBalProcessor(eventSource, config, nameSource);
        processor.registerPostProcessor(reportSerializer);
        final AggregatedBalanceReport testReport = processor.process();
        return testReport;
    }

    // it's not needed as we can easily use MultiUser* wrapper
    public BalanceReport singleUserVisualization(String user) throws IOException {
        config.setSubjects(List.of(user));
        final DataWriter<BalanceReport> reportWriter = Helper.getReportWriter(appProperties.getReportOutputType(), config);
        final List<PostProcessor<AbstractEvent, BalanceReport>> postProcessors = List.of(
                new TradeCountPostProcessor(),
                new NamePostProcessor(nameSource, config),
                new ReportSerializer(reportWriter)
        );
        final var processor = new FuturesBalanceProcessor(eventSource, config);
        processor.registerPostProcessor(postProcessors);

        final BalanceReport testReport = processor.process();
        return testReport;
    }
}
