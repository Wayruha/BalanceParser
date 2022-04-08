package com.example.binanceparser.report.processor;

import com.example.binanceparser.config.Config;
import com.example.binanceparser.report.BalanceReport;
import lombok.Getter;

import java.util.List;

public abstract class PostProcessor<Data> {
    @Getter
    protected final Config config;

    public PostProcessor(Config config) {
        this.config = config;
    }

    public PostProcessor() {
        this.config = null;
    }

    public abstract void processReport(BalanceReport report, List<Data> events);
}
