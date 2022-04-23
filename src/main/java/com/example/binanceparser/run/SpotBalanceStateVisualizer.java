package com.example.binanceparser.run;

import com.example.binanceparser.config.spring.BeanNames;
import com.example.binanceparser.processor.MultiUserGenericProcessor;
import com.example.binanceparser.report.AggregatedBalanceReport;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SpotBalanceStateVisualizer {
//    private final AppProperties appProperties;
//    private final BalanceVisualizerConfig config;
//    private final DataSource<AbstractEvent> eventSource;
//    private final DataSource<UserNameRel> nameSource;
    private final MultiUserGenericProcessor processor;

    public SpotBalanceStateVisualizer(
//            AppProperties appProperties, BalanceVisualizerConfig config,
//                                      DataSource<AbstractEvent> eventSource, DataSource<UserNameRel> nameSource,
                                      @Qualifier(BeanNames.SPOT_BALANCE_MULTIUSER_PROCESSOR) MultiUserGenericProcessor processor) {
//        this.appProperties = appProperties;
//        this.config = config;
//        this.eventSource = eventSource;
//        this.nameSource = nameSource;
        this.processor = processor;
    }

    public static void main(String[] args) throws IOException {
//        final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/spot-balance.properties");
//
//        final BalanceVisualizerConfig config = ConfigUtil.loadVisualizerConfig(appProperties);
//        final DataSource<AbstractEvent> eventSource = Helper.getEventSource(appProperties.getDataSourceType(), config);
//        final DataSource<UserNameRel> nameSource = Helper.getNameSource(appProperties.getDataSourceType(), config);
//        final SpotBalanceStateVisualizer visualizer = new SpotBalanceStateVisualizer(appProperties, config, eventSource, nameSource);
//
//        final AggregatedBalanceReport report = visualizer.spotBalanceVisualisation();
//        report.getReports().forEach(r -> System.out.println(r.toPrettyString() + "\n"));

    }

    public AggregatedBalanceReport spotBalanceVisualisation() throws IOException {
        //final DataWriter<BalanceReport> reportWriter = Helper.getReportWriter(appProperties.getReportOutputType(), config);
        //final var processor = new MultipleUsersSpotBalProcessor(eventSource, config, nameSource);
        //final var reportSerializer = new AggregatedBalReportSerializer(reportWriter);
        //processor.registerPostProcessor(reportSerializer);
        final AggregatedBalanceReport testReport = processor.process();
        return testReport;
    }
}