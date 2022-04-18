package com.example.binanceparser.report.postprocessor;

import com.example.binanceparser.config.Config;
import lombok.Getter;

import java.util.List;

public abstract class PostProcessor<Data, Report> {
    @Getter
    protected final Config config;

    public PostProcessor(Config config) {
        this.config = config;
    }

    public PostProcessor() {
        this.config = null;
    }

    public abstract void processReport(Report report, List<Data> events);
}
