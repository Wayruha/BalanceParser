package com.example.binanceparser.processor;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.sources.InMemorySource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.AggregatedBalanceReport;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.postprocessor.NamePostProcessor;
import com.example.binanceparser.report.postprocessor.SpotBalanceStateSerializer;
import com.example.binanceparser.report.postprocessor.TradeCountPostProcessor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MultiUserGenericProcessor extends Processor<AbstractEvent, AggregatedBalanceReport> {
    private static final Logger log = Logger.getLogger(MultipleUsersFuturesBalProcessor.class.getName());

    private final DataSource<UserNameRel> nameSource;
    private final BalanceVisualizerConfig config;
    private final ProcessorType type;

    public MultiUserGenericProcessor(ProcessorType type, DataSource<AbstractEvent> eventSource, BalanceVisualizerConfig config,
                                     DataSource<UserNameRel> nameSource) {
        super(eventSource);
        this.type = type;
        this.nameSource = nameSource;
        this.config = config;
    }

    @Override
    protected AggregatedBalanceReport process(List<AbstractEvent> events) {
        final List<BalanceReport> reports = new ArrayList<>();
        final List<String> users = config.getSubjects();
        final List<UserNameRel> nameRelations = nameSource != null ? nameSource.getData() : List.of();

        users.forEach(user -> {
            final SourceFilter nameFilter = new SourceFilter(user);
            final var eventSource = new InMemoryEventSource(events.stream(), nameFilter);

            final Processor<AbstractEvent, BalanceReport> processor;

            try {
                processor = buildProcessorByType(type, eventSource, config, user);
                processor.registerPostProcessor(new TradeCountPostProcessor());

                if (!nameRelations.isEmpty()) {
                    final var nameSource = new InMemorySource<>(nameRelations.stream());
                    processor.registerPostProcessor(new NamePostProcessor(nameSource, config));
                }

                final BalanceReport report = processor.process();
                reports.add(report);

            } catch (FileNotFoundException e) {
                log.log(Level.SEVERE, "Can't build processor for " + user, e);
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Can't prepare report for " + user, ex);
            }
        });

        return new AggregatedBalanceReport(reports);
    }

    private Processor<AbstractEvent, BalanceReport> buildProcessorByType(ProcessorType type, DataSource<AbstractEvent> dataSource, BalanceVisualizerConfig config, String user) throws FileNotFoundException {
        Processor<AbstractEvent, BalanceReport> processor;
        if (type == ProcessorType.FUTURES) {
            processor = new FuturesBalanceProcessor(dataSource, config);
        } else {
            processor = new SpotBalanceProcessor(dataSource, config);
        }

        if (type == ProcessorType.SPOT) {
            processor.registerPostProcessor(new SpotBalanceStateSerializer(new FileOutputStream(config.getReportOutputDir() + "/" + user + "_points.csv")));
        }
        return processor;
    }

    protected enum ProcessorType {
        SPOT, FUTURES;
    }
}
