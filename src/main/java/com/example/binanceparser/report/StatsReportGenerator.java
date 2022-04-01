package com.example.binanceparser.report;

import com.example.binanceparser.config.StatsVisualizerConfig;
import com.example.binanceparser.domain.Stats;
import com.example.binanceparser.plot.StatsChartBuilder;
import com.example.binanceparser.report.generator.FuturesBalanceReportGenerator;
import org.jfree.chart.JFreeChart;

import java.io.IOException;

public class StatsReportGenerator {
    private StatsChartBuilder chartBuilder;
    private StatsVisualizerConfig config;
    private static final String FORMAT = ".jpg";

    public StatsReportGenerator(StatsVisualizerConfig config) {
        this.config = config;
        chartBuilder = new StatsChartBuilder();
    }

    public StatsReport getStatsReport(Stats stats) throws IOException {
        StatsReport report = new StatsReport();
        JFreeChart chart;
        chart = chartBuilder.buildDelayChart(stats);
        report.setDelayChartPath(FuturesBalanceReportGenerator.saveChartToFile(chart, config.getOutputDir() + "\\delayDeviationChart" + FORMAT));
        chart = chartBuilder.buildPriceDeviationChart(stats);
        report.setPriceChartPath(FuturesBalanceReportGenerator.saveChartToFile(chart, config.getOutputDir() + "\\priceDeviationChart" + FORMAT));
        chart = chartBuilder.buildIncomeDeviationChart(stats);
        report.setIncomeChartPath(FuturesBalanceReportGenerator.saveChartToFile(chart, config.getOutputDir() + "\\incomeDeviationChart" + FORMAT));
        return report;
    }
}
