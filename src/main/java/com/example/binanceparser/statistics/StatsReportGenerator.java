package com.example.binanceparser.statistics;

import com.example.binanceparser.config.StatsVisualizerConfig;
import org.jfree.chart.JFreeChart;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

import static com.example.binanceparser.report.generator.FuturesBalanceReportGenerator.saveChartToFile;

@Service
class StatsReportGenerator {
    private static final String CHART_NAME_SUFFIX = ".jpg";
    private static final String CHART_NAME_PREFIX = "";
    private final StatsChartBuilder chartBuilder;
    private final StatsVisualizerConfig config;

    public StatsReportGenerator(StatsVisualizerConfig config, StatsChartBuilder chartBuilder) {
        this.config = config;
        this.chartBuilder = chartBuilder;//new StatsChartBuilder();
    }

    public StatsReport getStatisticFor(StatsDataHolder stats, StatisticType type) {
        JFreeChart chart = null;
        List dataset = null;

        switch (type) {
            case DELAY:
                dataset = stats.getDelayData();
                chart = chartBuilder.buildDelayChart(dataset, config.getDelayPrecision());
                break;
            case ORDER_PRICE_DEVIATION:
                dataset = stats.getOrderPriceData();
                chart = chartBuilder.buildPriceDeviationChart(dataset);
                break;
            case POSITION_PROFIT_DEVIATION:
                dataset = stats.getPositionProfitData();
                chart = chartBuilder.buildIncomeDeviationChart(dataset);
        }
        final StatsReport report = new StatsReport(type);
        final File chartPath = new File(config.getOutputDir(), CHART_NAME_PREFIX + type + CHART_NAME_SUFFIX);
        saveChartToFile(chart, chartPath.getAbsolutePath());

        report.setChartPath(chartPath.getAbsolutePath());
        report.setStats(new Statistics(dataset));

        return report;
    }
}
