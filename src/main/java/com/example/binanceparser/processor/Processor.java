package com.example.binanceparser.processor;

import com.example.binanceparser.config.Config;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.sources.EventSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.processor.PostProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Processor<T extends Config, Data> {
    protected final T config;
    protected final DataSource<Data> dataSource;
    protected final Set<PostProcessor<Data>> postProcessors;

    public Processor(T config, DataSource<Data> dataSource) {
        this.config = config;
        this.dataSource = dataSource;
        this.postProcessors = new HashSet<>();
    }

    public BalanceReport process() {
        final List<Data> data = dataSource.getData();
        final BalanceReport report = process(data);
        postProcessors.forEach(pp -> pp.processReport(report, data));
        return report;
    }

    abstract protected BalanceReport process(List<Data> data);

    public boolean registerPostProcessor(PostProcessor<Data> postProcessor) {
        return registerPostProcessor(List.of(postProcessor));
    }

    public boolean registerPostProcessor(List<PostProcessor<Data>> _postProcessors) {
        return postProcessors.addAll(_postProcessors);
    }

    public boolean removePostProcessor(PostProcessor<Data> postProcessor) {
        return postProcessors.remove(postProcessor);
    }

    public void removePostProcessors() {
        postProcessors.clear();
    }
}
