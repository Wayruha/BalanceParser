package com.example.binanceparser.processor;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.domain.events.AbstractEvent;

public class MultipleUsersFuturesBalProcessor extends MultiUserGenericProcessor {

    public MultipleUsersFuturesBalProcessor(DataSource<AbstractEvent> eventSource, BalanceVisualizerConfig config, DataSource<UserNameRel> nameSource) {
        super(ProcessorType.FUTURES, eventSource, config, nameSource);
    }
}
