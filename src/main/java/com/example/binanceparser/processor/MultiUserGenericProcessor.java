package com.example.binanceparser.processor;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.filters.SourceFilter;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.sources.InMemorySource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.processor.NamePostProcessor;
import com.example.binanceparser.report.processor.TradeCountPostProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MultiUserGenericProcessor extends Processor<AbstractEvent, List<BalanceReport>> {
    private static final Logger log = Logger.getLogger(MultipleUsersFuturesBalProcessor.class.getName());

    private final DataSource<UserNameRel> nameSource;
    private final BalanceVisualizerConfig config;
    private final ProcessorType type;

    public MultiUserGenericProcessor(ProcessorType type, DataSource<AbstractEvent> eventSource, BalanceVisualizerConfig config, DataSource<UserNameRel> nameSource) {
        super(eventSource);
        this.type = type;
        this.nameSource = nameSource;
        this.config = config;
    }

    @Override
    protected List<BalanceReport> process(List<AbstractEvent> events) {
        final List<BalanceReport> reports = new ArrayList<>();
        final List<String> users = config.getSubjects();
        final List<UserNameRel> nameRelations = nameSource.getData();

        users.forEach(user -> {
            final SourceFilter nameFilter = new SourceFilter(user);
            final var eventSource = new InMemoryEventSource(events.stream(), nameFilter);
            final var nameSource = new InMemorySource<>(nameRelations.stream());

            final var processor = buildProcessorByType(type, eventSource, config);

            processor.registerPostProcessor(new TradeCountPostProcessor());
            processor.registerPostProcessor(new NamePostProcessor(nameSource, config));

            try {
                final BalanceReport report = processor.process();
                reports.add(report);
            } catch (Exception ex){
                log.log(Level.SEVERE, "Can't prepare report for " + user, ex);
            }
        });

        return reports;
    }

    private Processor<AbstractEvent, BalanceReport> buildProcessorByType(ProcessorType type, DataSource<AbstractEvent> dataSource, BalanceVisualizerConfig config){
        if(type == ProcessorType.FUTURES) return new FuturesBalanceProcessor(dataSource, config);
        return new SpotBalanceProcessor(dataSource, config);
    }

    protected enum ProcessorType {
        SPOT, FUTURES;
    }
}
