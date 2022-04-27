package com.example.binanceparser.processor;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.CSVEventSource;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.sources.InMemorySource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.AggregatedBalanceReport;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.postprocessor.NamePostProcessor;
import com.example.binanceparser.report.postprocessor.TradeCountPostProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        List<String> users = config.getSubjects();
        final List<UserNameRel> nameRelations = nameSource != null ? nameSource.getData() : List.of();

        if (users.isEmpty()) {
            final DataSource<AbstractEvent> redundantEventSource = new CSVEventSource(new File(config.getInputFilepath()), Collections.emptyList());
            users = extractAllUsersIds(redundantEventSource);
        }

        users.forEach(user -> {
            final SourceFilter nameFilter = new SourceFilter(user);
            final var eventSource = new InMemoryEventSource(events.stream(), nameFilter);

            final var processor = buildProcessorByType(type, eventSource, config);

            processor.registerPostProcessor(new TradeCountPostProcessor());
            if (!nameRelations.isEmpty()) {
                final var nameSource = new InMemorySource<>(nameRelations.stream());
                processor.registerPostProcessor(new NamePostProcessor(nameSource, config));
            }

            try {
                final BalanceReport report = processor.process();
                reports.add(report);
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Can't prepare report for " + user, ex);
            }
        });

        return new AggregatedBalanceReport(reports);
    }

    private Processor<AbstractEvent, BalanceReport> buildProcessorByType(ProcessorType type, DataSource<AbstractEvent> dataSource, BalanceVisualizerConfig config) {
        if (type == ProcessorType.FUTURES) return new FuturesBalanceProcessor(dataSource, config);
        else if (type != ProcessorType.SPOT)
            throw new UnsupportedOperationException("type:" + type + " is not allowed here");
        return new SpotBalanceProcessor(dataSource, config);
    }

    private List<String> extractAllUsersIds(DataSource<AbstractEvent> redundantEventSource) {
        return redundantEventSource.getData().stream()
                .map(AbstractEvent::getSource)
                .distinct()
                .collect(Collectors.toList());
    }
}
