package com.example.binanceparser.run;

import com.example.binanceparser.processor.MultiUserGenericProcessor;
import com.example.binanceparser.report.AggregatedBalanceReport;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.logging.Logger;

@Service
public class FuturesBalanceStateVisualizer {
    private static final Logger log = Logger.getLogger(FuturesBalanceStateVisualizer.class.getName());
//
//    private final AppProperties appProperties;
//    private final BalanceVisualizerConfig config;
//    private final DataSource<AbstractEvent> eventSource;
//    private final DataSource<UserNameRel> nameSource;

    private final MultiUserGenericProcessor processor;

    public FuturesBalanceStateVisualizer(//AppProperties appProperties, BalanceVisualizerConfig config,
                                         //DataSource<AbstractEvent> eventSource, DataSource<UserNameRel> nameSource,
                                         MultiUserGenericProcessor processor) {
//        this.appProperties = appProperties;
//        this.config = config;
//        this.eventSource = eventSource;
//        this.nameSource = nameSource;
        this.processor = processor;
    }

//    public static void main(String[] args) throws IOException {
//        final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/futures-balance.properties");
//
//        final BalanceVisualizerConfig config = ConfigUtil.loadVisualizerConfig(appProperties);
//        final DataSource<AbstractEvent> eventSource = Helper.getEventSource(appProperties.getDataSourceType(), config);
//        final DataSource<UserNameRel> nameSource = Helper.getNameSource(appProperties.getDataSourceType(), config);
//        final FuturesBalanceStateVisualizer visualizer = new FuturesBalanceStateVisualizer(appProperties, config, eventSource, nameSource);
//
//        final AggregatedBalanceReport reports = visualizer.futuresBalanceVisualisation();
//        reports.getReports().forEach(r -> System.out.println(r.toPrettyString() + "\n"));
//    }

    public AggregatedBalanceReport futuresBalanceVisualisation() throws IOException {
//        final DataWriter<BalanceReport> reportWriter = Helper.getReportWriter(appProperties.getReportOutputType(), config);
//        final var reportSerializer = new AggregatedBalReportSerializer(reportWriter);
//
//        final var processor = new MultipleUsersFuturesBalProcessor(eventSource, config, nameSource);
//        processor.registerPostProcessor(reportSerializer);
        final AggregatedBalanceReport testReport = processor.process();
        testReport.getReports().forEach(r -> System.out.println(r.toPrettyString() + "\n"));
        return testReport;
    }

//    // it's not needed as we can easily use MultiUser* wrapper
//    public BalanceReport singleUserVisualization(String user) throws IOException {
//        config.setSubjects(List.of(user));
//        final DataWriter<BalanceReport> reportWriter = Helper.getReportWriter(appProperties.getReportOutputType(), config);
//        final List<PostProcessor<AbstractEvent, BalanceReport>> postProcessors = List.of(
//                new TradeCountPostProcessor(),
//                new NamePostProcessor(nameSource, config),
//                new ReportSerializer(reportWriter)
//        );
//        final var processor = new FuturesBalanceProcessor(eventSource, config);
//        processor.registerPostProcessor(postProcessors);
//
//        final BalanceReport testReport = processor.process();
//        return testReport;
//    }
}
