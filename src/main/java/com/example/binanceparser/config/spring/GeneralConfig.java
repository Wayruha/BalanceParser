package com.example.binanceparser.config.spring;

import com.example.binanceparser.AppProperties;
import com.example.binanceparser.Constants;
import com.example.binanceparser.config.BalanceVisualizerConfig;
import com.example.binanceparser.config.ConfigUtil;
import com.example.binanceparser.config.IncomeConfig;
import com.example.binanceparser.datasource.models.UserNameRel;
import com.example.binanceparser.datasource.sources.DataSource;
import com.example.binanceparser.domain.events.AbstractEvent;
import com.example.binanceparser.run.Helper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.IOException;

import static com.example.binanceparser.config.spring.BeanNames.*;

@Configuration
public class GeneralConfig {
    @Bean(FUTURES_PROPS)
    public AppProperties futuresProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.FUTURES_PROPS_PATH);
    }

    @Bean(INCOME_PROPS)
    public AppProperties incomeProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.FUTURES_INCOME_PROPS_PATH);
    }

    @Bean(SPOT_PROPS)
    public AppProperties spotProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.SPOT_PROPS_PATH);
    }

    @Bean(STATS_PROPS)
    public AppProperties statsProps() throws IOException {
        return ConfigUtil.loadAppProperties(Constants.STATS_PROPS_PATH);
    }

//    @Bean(BeanNames.TRADES_COMPARATOR_PROPS)
//    public AppProperties comparatorProps() throws IOException {
//        return ConfigUtil.loadAppProperties(Constants.TRADES_COMPARATOR_PROPS_PATH);
//    }
//
    @Bean(name = FUTURES_CONFIG)
    @DependsOn({FUTURES_PROPS})
    public BalanceVisualizerConfig futuresConfig(@Qualifier(FUTURES_PROPS) AppProperties futuresBalanceProperties) {
        return ConfigUtil.loadVisualizerConfig(futuresBalanceProperties);
    }

    @Bean(name = INCOME_CONFIG)
    @DependsOn({INCOME_PROPS})
    public IncomeConfig futuresIncomeConfig(@Qualifier(INCOME_PROPS) AppProperties futuresIncomeProperties) {
        return ConfigUtil.loadIncomeConfig(futuresIncomeProperties);
    }

    @Bean(name = SPOT_CONFIG)
    @DependsOn({SPOT_PROPS})
    public BalanceVisualizerConfig spotConfig(@Qualifier(SPOT_PROPS) AppProperties spotBalanceProperties) {
        return ConfigUtil.loadVisualizerConfig(spotBalanceProperties);
    }
//
//    @Bean(name = BeanNames.TRADES_COMPARATOR_CONFIG)
//    @DependsOn({"comparatorProps"})
//    public BalanceVisualizerConfig comparatorConfig(@Autowired AppProperties comparatorConfig) {
//        return ConfigUtil.loadVisualizerConfig(comparatorConfig);
//    }
//
//    @Bean(name = BeanNames.STATS_CONFIG)
//    @DependsOn({"statsProps", "statsFilters"})
//    public StatsVisualizerConfig statsConfig(@Autowired AppProperties statsBalanceProperties, @Autowired Set<Filter> filters) throws FileNotFoundException {
//        List<String> users = statsBalanceProperties.getTrackedPersons();
//        if (users.isEmpty()) {
//            users = new CSVEventSource(new File(statsBalanceProperties.getInputFilePath()), statsBalanceProperties.getTrackedPersons()).getUserIds();
//        }
//        StatsVisualizerConfig config = ConfigUtil.loadStatsConfig(statsBalanceProperties);
//        config.setFilters(filters);
//        config.setSubjects(users);
//        return ConfigUtil.loadStatsConfig(statsBalanceProperties);
//    }

//    @Bean(BeanNames.STATS_EVENT_SOURCE)
//    @DependsOn({"statsProps", "statsConfig", "statsFilters"})
//    public DataSource<AbstractEvent> statsEventSource(@Autowired AppProperties appProperties, @Autowired StatsVisualizerConfig config, @Autowired Set<Filter> filters) {
//        final File logsDir = new File(config.getInputFilepath());
//        DataSource<AbstractEvent> eventSource;
//        switch (appProperties.getDataSourceType()) {
//            case CSV:
//                eventSource = new CSVEventSource(logsDir, config.getSubjects());
//                break;
//            case LOGS:
//                eventSource = new LogsEventSource(logsDir, filters);
//                break;
//            default:
//                throw new RuntimeException("unknown event source type specified");
//        }
//        return eventSource;
//    }

//    @Bean(name = BeanNames.SPOT_REPORT_WRITER)
//    @DependsOn({"spotProps", "spotConfig"})
//    public DataWriter<BalanceReport> spotReportWriter(@Autowired AppProperties appProperties, @Autowired BalanceVisualizerConfig config) throws IOException {
//        return Helper.getReportWriter(appProperties.getReportOutputType(), config);
//    }
//
//    @Bean(name = BeanNames.FUTURES_REPORT_WRITER)
//    @DependsOn({"futuresProps", "futuresConfig"})
//    public DataWriter<BalanceReport> futuresReportWriter(@Autowired AppProperties appProperties, @Autowired BalanceVisualizerConfig config) throws IOException {
//        return Helper.getReportWriter(appProperties.getReportOutputType(), config);
//    }

//    @Bean(name = BeanNames.SPOT_REPORT_SERIALIZER)
//    @DependsOn({"spotReportWriter"})
//    public AggregatedBalReportSerializer spotReportSerializer(@Autowired DataWriter<BalanceReport> reportWriter) {
//        return new AggregatedBalReportSerializer(reportWriter);
//    }
//
//    @Bean(name = BeanNames.FUTURES_REPORT_SERIALIZER)
//    @DependsOn({"futuresReportWriter"})
//    public AggregatedBalReportSerializer futuresReportSerializer(@Autowired DataWriter<BalanceReport> reportWriter) {
//        return new AggregatedBalReportSerializer(reportWriter);
//    }

//    @Bean(name = BeanNames.SPOT_BALANCE_MULTIUSER_PROCESSOR)
//    @DependsOn({"spotReportSerializer", "spotConfig", "spotEventSource", "spotNameSource"})
//    public MultiUserGenericProcessor spotGenericProcessor(@Autowired AggregatedBalReportSerializer reportSerializer, @Autowired BalanceVisualizerConfig config, @Autowired DataSource<AbstractEvent> eventSource, @Autowired DataSource<UserNameRel> spotNameSource) {
//        MultiUserGenericProcessor processor = new MultipleUsersSpotBalProcessor(eventSource, config, spotNameSource);
//        processor.registerPostProcessor(reportSerializer);
//        return processor;
//    }
//
//    @Bean(name = BeanNames.FUTURES_BALANCE_MULTIUSER_PROCESSOR)
//    @DependsOn({"futuresReportSerializer", "futuresConfig", "futuresEventSource", "futuresNameSource"})
//    public MultiUserGenericProcessor futuresGenericProcessor(@Autowired AggregatedBalReportSerializer reportSerializer, @Autowired BalanceVisualizerConfig config, @Autowired DataSource<AbstractEvent> eventSource, @Autowired DataSource<UserNameRel> futuresNameSource) {
//        MultiUserGenericProcessor processor = new MultipleUsersFuturesBalProcessor(eventSource, config, futuresNameSource);
//        processor.registerPostProcessor(reportSerializer);
//        return processor;
//    }

    @Bean(SPOT_EVENT_SOURCE)
    @DependsOn({SPOT_PROPS, SPOT_CONFIG})
    public DataSource<AbstractEvent> spotEventSource(@Qualifier(SPOT_PROPS) AppProperties appProperties, @Qualifier(SPOT_CONFIG) BalanceVisualizerConfig config) {
        return Helper.getEventSource(appProperties.getDataSourceType(), config);
    }

    @Bean(FUTURES_EVENT_SOURCE)
    @DependsOn({FUTURES_PROPS, FUTURES_CONFIG})
    public DataSource<AbstractEvent> futuresEventSource(@Qualifier(FUTURES_PROPS) AppProperties appProperties, @Qualifier(FUTURES_CONFIG) BalanceVisualizerConfig config) {
        return Helper.getEventSource(appProperties.getDataSourceType(), config);
    }

//    @Bean(TRADES_COMPARATOR_EVENT_SOURCE)
//    @DependsOn({"comparatorProps", "comparatorConfig"})
//    public DataSource<AbstractEvent> comparatorEventSource(@Autowired AppProperties appProperties, @Autowired BalanceVisualizerConfig config) {
//        return Helper.getEventSource(appProperties.getDataSourceType(), config);
//    }

    @Bean(SPOT_NAME_SOURCE)
    @DependsOn({SPOT_PROPS, SPOT_CONFIG})
    public DataSource<UserNameRel> spotNameSource(@Qualifier(SPOT_PROPS) AppProperties appProperties, @Qualifier(SPOT_CONFIG) BalanceVisualizerConfig config) {
        return Helper.getNameSource(appProperties.getDataSourceType(), config);
    }

    @Bean(FUTURES_NAMES_SOURCE)
    @DependsOn({FUTURES_PROPS, FUTURES_CONFIG})
    public DataSource<UserNameRel> futuresNameSource(@Qualifier(FUTURES_PROPS) AppProperties appProperties, @Qualifier(FUTURES_CONFIG) BalanceVisualizerConfig config) {
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

//    @Bean(STATS_FILTERS)
//    public Set<Filter> statsFilters() {
//        final Set<Filter> filters = new HashSet<>();
//        filters.add(new FuturesOrderStatusFilter(List.of(OrderStatus.FILLED, OrderStatus.PARTIALLY_FILLED)));
//        filters.add(new ReduceOnlyFilter(true));
//        return filters;
//    }
}
