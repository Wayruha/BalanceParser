package com.example.binanceparser.report.generator;

import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.domain.StatsDataHolder;
import com.example.binanceparser.plot.StatsChartBuilder;
import com.example.binanceparser.report.StatisticType;
import com.example.binanceparser.report.Statistics;
import com.example.binanceparser.report.StatsReport;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.example.binanceparser.report.generator.FuturesBalanceReportGenerator.saveChartToFile;

public class StatsReportGenerator {
    private static final String CHART_NAME_SUFFIX = ".jpg";
    private static final String CHART_NAME_PREFIX = "";
    private final StatsChartBuilder chartBuilder;
    private final StatsVisualizerConfig config;


    public StatsReportGenerator(StatsVisualizerConfig config) {
        this.config = config;
        chartBuilder = new StatsChartBuilder();
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
        saveChart(chart, chartPath);

        report.setChartPath(chartPath.getAbsolutePath());
        report.setStats(new Statistics(dataset));

        return report;
    }

    //TODO це знак, що генерація графіку не повинна бути в генераторі репорту!
    private void saveChart(JFreeChart chart, File chartPath) {
        try {
            saveChartToFile(chart, chartPath);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
