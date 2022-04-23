package com.example.binanceparser.config.spring;

import com.binance.api.client.domain.OrderStatus;
import com.example.binanceparser.AppProperties;
import com.example.binanceparser.Constants;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.datasource.filters.Filter;
import com.example.binanceparser.datasource.filters.FuturesOrderStatusFilter;
import com.example.binanceparser.datasource.filters.ReduceOnlyFilter;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.CSVEventSource;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.datasource.sources.LogsEventSource;
import com.example.binanceparser.datasource.writers.DataWriter;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.processor.MultiUserGenericProcessor;
import com.example.binanceparser.processor.MultipleUsersFuturesBalProcessor;
import com.example.binanceparser.processor.MultipleUsersSpotBalProcessor;
import com.example.binanceparser.processor.Processor;
import com.example.binanceparser.report.BalanceReport;
import com.example.binanceparser.report.postprocessor.AggregatedBalReportSerializer;
import com.example.binanceparser.run.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
//@ComponentScan
public class GeneralConfig {
    @Bean(BeanNames.FUTURES_PROPS)
    public AppProperties futuresProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.FUTURES_PROPS_PATH);
    }

    @Bean(BeanNames.INCOME_PROPS)
    public AppProperties incomeProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.FUTURES_INCOME_PROPS_PATH);
    }

    @Bean(BeanNames.SPOT_PROPS)
    public AppProperties spotProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.SPOT_PROPS_PATH);
    }

    @Bean(BeanNames.STATS_PROPS)
    public AppProperties statsProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.STATS_PROPS_PATH);
    }

    @Bean(BeanNames.TRADES_COMPARATOR_PROPS)
    public AppProperties comparatorProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.TRADES_COMPARATOR_PROPS_PATH);
    }

    @Bean(name = BeanNames.FUTURES_CONFIG)
    @DependsOn({"futuresProps"})
    public BalanceVisualizerConfig futuresConfig(@Autowired AppProperties futuresBalanceProperties) {
        return ConfigUtil.loadVisualizerConfig(futuresBalanceProperties);
    }

    @Bean(name = BeanNames.INCOME_CONFIG)
    @DependsOn({"futuresProps"})
    public IncomeConfig futuresIncomeConfig(@Autowired AppProperties futuresIncomeProperties) {
        return ConfigUtil.loadIncomeConfig(futuresIncomeProperties);
    }

    @Bean(name = BeanNames.SPOT_CONFIG)
    @DependsOn({"spotProps"})
    public BalanceVisualizerConfig spotConfig(@Autowired AppProperties spotBalanceProperties) {
        return ConfigUtil.loadVisualizerConfig(spotBalanceProperties);
    }

    @Bean(name = BeanNames.TRADES_COMPARATOR_CONFIG)
    @DependsOn({"comparatorProps"})
    public BalanceVisualizerConfig comparatorConfig(@Autowired AppProperties comparatorConfig) {
        return ConfigUtil.loadVisualizerConfig(comparatorConfig);
    }

    @Bean(name = BeanNames.STATS_CONFIG)
    @DependsOn({"statsProps", "statsFilters"})
    public StatsVisualizerConfig statsConfig(@Autowired AppProperties statsBalanceProperties, @Autowired Set<Filter> filters) throws FileNotFoundException {
        List<String> users = statsBalanceProperties.getTrackedPersons();
        if (users.isEmpty()) {
            users = new CSVEventSource(new File(statsBalanceProperties.getInputFilePath()), statsBalanceProperties.getTrackedPersons()).getUserIds();
        }
        StatsVisualizerConfig config = ConfigUtil.loadStatsConfig(statsBalanceProperties);
        config.setFilters(filters);
        config.setSubjects(users);
        return ConfigUtil.loadStatsConfig(statsBalanceProperties);
    }

    @Bean(BeanNames.STATS_EVENT_SOURCE)
    @DependsOn({"statsProps", "statsConfig", "statsFilters"})
    public DataSource<AbstractEvent> statsEventSource(@Autowired AppProperties appProperties, @Autowired StatsVisualizerConfig config, @Autowired Set<Filter> filters) {
        final File logsDir = new File(config.getInputFilepath());
        DataSource<AbstractEvent> eventSource;
        switch (appProperties.getDataSourceType()) {
            case CSV:
                eventSource = new CSVEventSource(logsDir, config.getSubjects());
                break;
            case LOGS:
                eventSource = new LogsEventSource(logsDir, filters);
                break;
            default:
                throw new RuntimeException("unknown event source type specified");
        }
        return eventSource;
    }

    @Bean(name = BeanNames.SPOT_REPORT_WRITER)
    @DependsOn({"spotProps", "spotConfig"})
    public DataWriter<BalanceReport> spotReportWriter(@Autowired AppProperties appProperties, @Autowired BalanceVisualizerConfig config) throws IOException {
        return Helper.getReportWriter(appProperties.getReportOutputType(), config);
    }

    @Bean(name = BeanNames.FUTURES_REPORT_WRITER)
    @DependsOn({"futuresProps", "futuresConfig"})
    public DataWriter<BalanceReport> futuresReportWriter(@Autowired AppProperties appProperties, @Autowired BalanceVisualizerConfig config) throws IOException {
        return Helper.getReportWriter(appProperties.getReportOutputType(), config);
    }

    @Bean(name = BeanNames.SPOT_REPORT_SERIALIZER)
    @DependsOn({"spotReportWriter"})
    public AggregatedBalReportSerializer spotReportSerializer(@Autowired DataWriter<BalanceReport> reportWriter) {
        return new AggregatedBalReportSerializer(reportWriter);
    }

    @Bean(name = BeanNames.FUTURES_REPORT_SERIALIZER)
    @DependsOn({"futuresReportWriter"})
    public AggregatedBalReportSerializer futuresReportSerializer(@Autowired DataWriter<BalanceReport> reportWriter) {
        return new AggregatedBalReportSerializer(reportWriter);
    }

    @Bean(name = BeanNames.SPOT_BALANCE_MULTIUSER_PROCESSOR)
    @DependsOn({"spotReportSerializer", "spotConfig", "spotEventSource", "spotNameSource"})
    public MultiUserGenericProcessor spotGenericProcessor(@Autowired AggregatedBalReportSerializer reportSerializer, @Autowired BalanceVisualizerConfig config, @Autowired DataSource<AbstractEvent> eventSource, @Autowired DataSource<UserNameRel> spotNameSource) {
        MultiUserGenericProcessor processor = new MultipleUsersSpotBalProcessor(eventSource, config, spotNameSource);
        processor.registerPostProcessor(reportSerializer);
        return processor;
    }

    @Bean(name = BeanNames.FUTURES_BALANCE_MULTIUSER_PROCESSOR)
    @DependsOn({"futuresReportSerializer", "futuresConfig", "futuresEventSource", "futuresNameSource"})
    public MultiUserGenericProcessor futuresGenericProcessor(@Autowired AggregatedBalReportSerializer reportSerializer, @Autowired BalanceVisualizerConfig config, @Autowired DataSource<AbstractEvent> eventSource, @Autowired DataSource<UserNameRel> futuresNameSource) {
        MultiUserGenericProcessor processor = new MultipleUsersFuturesBalProcessor(eventSource, config, futuresNameSource);
        processor.registerPostProcessor(reportSerializer);
        return processor;
    }

    @Bean(BeanNames.SPOT_EVENT_SOURCE)
    @DependsOn({"spotProps", "spotConfig"})
    public DataSource<AbstractEvent> spotEventSource(@Autowired AppProperties appProperties, @Autowired BalanceVisualizerConfig config) {
        return Helper.getEventSource(appProperties.getDataSourceType(), config);
    }

    @Bean(BeanNames.FUTURES_EVENT_SOURCE)
    @DependsOn({"futuresProps", "futuresConfig"})
    public DataSource<AbstractEvent> futuresEventSource(@Autowired AppProperties appProperties, @Autowired BalanceVisualizerConfig config) {
        return Helper.getEventSource(appProperties.getDataSourceType(), config);
    }

    @Bean(BeanNames.TRADES_COMPARATOR_EVENT_SOURCE)
    @DependsOn({"comparatorProps", "comparatorConfig"})
    public DataSource<AbstractEvent> comparatorEventSource(@Autowired AppProperties appProperties, @Autowired BalanceVisualizerConfig config) {
        return Helper.getEventSource(appProperties.getDataSourceType(), config);
    }

    @Bean(BeanNames.SPOT_NAME_SOURCE)
    @DependsOn({"spotProps", "spotConfig"})
    public DataSource<UserNameRel> spotNameSource(@Autowired AppProperties appProperties, @Autowired BalanceVisualizerConfig config) {
        return Helper.getNameSource(appProperties.getDataSourceType(), config);
    }

    @Bean(BeanNames.FUTURES_NAMES_SOURCE)
    @DependsOn({"futuresProps", "futuresConfig"})
    public DataSource<UserNameRel> futuresNameSource(@Autowired AppProperties appProperties, @Autowired BalanceVisualizerConfig config) {
        return Helper.getNameSource(appProperties.getDataSourceType(), config);
    }

//    @Bean(BeanNames.API_CLIENT_SOURCE)
//    @DependsOn({"incomeProps", "futuresIncomeConfig"})
//    public DataSource<IncomeHistoryItem> apiClientSource(@Autowired AppProperties appProperties, @Autowired IncomeConfig config) {
//        switch (appProperties.getHistoryItemSourceType()) {
//            case LOGS:
//                return new JsonIncomeSource(new File(config.getInputFilepath()));
//            case BINANCE:
//                config.setLimit(1000);
//                config.setSubjects(of(user.getUserId()));
//                config.setIncomeTypes(config.getIncomeTypes());
//                String apiKey = user.getApiKey();
//                String secretKey = user.getSecretKey();
//                return new BinanceIncomeDataSource(apiKey, secretKey, config);
//            default:
//                throw new UnsupportedOperationException();
//        }
//        return null;
//    }

    @Bean(BeanNames.STATS_FILTERS)
    public Set<Filter> statsFilters() {
        final Set<Filter> filters = new HashSet<>();
        filters.add(new FuturesOrderStatusFilter(List.of(OrderStatus.FILLED, OrderStatus.PARTIALLY_FILLED)));
        filters.add(new ReduceOnlyFilter(true));
        return filters;
    }

    public Processor<AbstractEvent, BalanceReport> futuresBalanceProcessor() {
        return null;
    }
}
