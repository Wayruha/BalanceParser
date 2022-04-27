package com.example.binanceparser.run.service;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.config.spring.BeanNames;
import com.example.binanceparser.processor.MultipleUsersIncomeBalProcessor;
import com.example.binanceparser.report.AggregatedBalanceReport;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(BeanNames.INCOME_RUNNER)
@AllArgsConstructor
public class IncomeRunner implements RunnerService {
    @Qualifier(BeanNames.INCOME_PROPS)
    private final AppProperties props;
    @Override
    public AggregatedBalanceReport getReport(List<String> trackedPersons) {
        IncomeConfig config = ConfigUtil.loadIncomeConfig(props);
        config.setSubjects(trackedPersons);
        var processor = new MultipleUsersIncomeBalProcessor(config, props.getHistoryItemSourceType());
        return processor.process();
    }
}
