package com.example.binanceparser.statistics;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class StatsVisualizer {
    private static final Logger log = Logger.getLogger(StatsVisualizer.class.getName());
    //private final AppProperties appProperties;
    private final StatsProcessor processor;

    public StatsVisualizer(
            //@Qualifier(BeanNames.STATS_PROPS) AppProperties appProperties,
            StatsProcessor processor) {
        //his.appProperties = appProperties;
        this.processor = processor;
    }

//    public static void main(String[] args) throws Exception {
//        final AppProperties appProperties = ConfigUtil.loadAppProperties("src/main/resources/stats-visualisation.properties");
//        final StatsVisualizer visualizer = new StatsVisualizer(appProperties);
//        List<String> users = appProperties.getTrackedPersons();
//        if (users.isEmpty()) {
//            users = new CSVEventSource(new File(appProperties.getInputFilePath()), appProperties.getTrackedPersons()).getUserIds();
//        }
//        final List<StatsReport> reports = visualizer.calculateStatistics(users);
//        reports.forEach(report -> {
//            log.info("Report: " + report.getType());
//            log.info(report.toString());
//        });
//    }

    public List<StatsReport> calculateStatistics(List<String> users) {
//        final StatsVisualizerConfig config = ConfigUtil.loadStatsConfig(appProperties);
//        config.setFilters(filters());
//        config.setSubjects(users);
//        final DataSource<AbstractEvent> eventSource = getEventSource(appProperties.getDataSourceType(), config);
//        final StatsProcessor processor = new StatsProcessor(config, eventSource);
        final List<StatsReport> reports = processor.process();
        reports.forEach(report -> {
            log.info("Report: " + report.getType());
            log.info(report.toString());
        });
        return reports;
    }

//    private static Set<Filter> filters() {
//        final Set<Filter> filters = new HashSet<>();
//        filters.add(new FuturesOrderStatusFilter(List.of(OrderStatus.FILLED, OrderStatus.PARTIALLY_FILLED)));
//        filters.add(new ReduceOnlyFilter(true));
//        return filters;
//    }

//    public static DataSource<AbstractEvent> getEventSource(AppProperties.DatasourceType datasourceType, Config config) {
//        final File logsDir = new File(config.getInputFilepath());
//        DataSource<AbstractEvent> eventSource;
//        switch (datasourceType) {
//            case CSV:
//                eventSource = new CSVEventSource(logsDir, config.getSubjects());
//                break;
//            case LOGS:
//                eventSource = new LogsEventSource(logsDir, filters());
//                break;
//            default:
//                throw new RuntimeException("unknown event source type specified");
//        }
//        return eventSource;
//    }
}
