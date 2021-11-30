package com.example.binanceparser.processor;

import com.example.binanceparser.config.Config;
import com.example.binanceparser.report.BalanceReport;

import java.io.IOException;

public abstract class Processor<T extends Config> {
    protected final T config;

    public Processor(T config) {
        this.config = config;
    }

    abstract public BalanceReport process() throws IOException;
}
