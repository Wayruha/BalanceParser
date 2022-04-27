package com.example.binanceparser.run.service;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.spring.BeanNames;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.MultiUserGenericProcessor;
import com.example.binanceparser.processor.MultipleUsersFuturesBalProcessor;
import com.example.binanceparser.report.AggregatedBalanceReport;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(BeanNames.FUTURES_RUNNER)
@AllArgsConstructor
public class FuturesRunner implements RunnerService {
    @Qualifier(BeanNames.FUTURES_PROPS)
    private final AppProperties props;
    @Qualifier(BeanNames.FUTURES_EVENT_SOURCE)
    private final DataSource<AbstractEvent> eventSource;
    @Qualifier(BeanNames.FUTURES_NAMES_SOURCE)
    private final DataSource<UserNameRel> userSource;
    @Override
    public AggregatedBalanceReport getReport(List<String> trackedPersons) {
        BalanceVisualizerConfig config = ConfigUtil.loadVisualizerConfig(props);
        config.setSubjects(trackedPersons);
        MultiUserGenericProcessor processor = new MultipleUsersFuturesBalProcessor(eventSource, config, userSource);
        return processor.process();
    }
}
