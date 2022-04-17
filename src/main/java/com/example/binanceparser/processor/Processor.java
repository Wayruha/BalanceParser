package com.example.binanceparser.processor;

import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.report.processor.PostProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Processor<Input, Output> {
    protected final DataSource<Input> dataSource;
    protected final Set<PostProcessor<Input, Output>> postProcessors;

    public Processor(DataSource<Input> dataSource) {
        this.dataSource = dataSource;
        this.postProcessors = new HashSet<>();
    }

    public Output process() {
        final List<Input> data = dataSource.getData();
        final Output report = process(data);
        postProcessors.forEach(pp -> pp.processReport(report, data));
        return report;
    }

    abstract protected Output process(List<Input> data);

    public boolean registerPostProcessor(PostProcessor<Input, Output> postProcessor) {
        return registerPostProcessor(List.of(postProcessor));
    }

    public boolean registerPostProcessor(List<PostProcessor<Input, Output>> _postProcessors) {
        return postProcessors.addAll(_postProcessors);
    }

    public boolean removePostProcessor(PostProcessor<Input, Output> postProcessor) {
        return postProcessors.remove(postProcessor);
    }

    public void removePostProcessors() {
        postProcessors.clear();
    }
}
