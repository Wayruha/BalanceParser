package com.example.binanceparser.processor;

import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.domain.events.AbstractEvent;

public class MultipleUsersSpotBalProcessor extends MultiUserGenericProcessor {
    public MultipleUsersSpotBalProcessor(DataSource<AbstractEvent> eventSource, BalanceVisualizerConfig config, DataSource<UserNameRel> nameSource) {
        super(ProcessorType.SPOT, eventSource, config, nameSource);
    }
}
