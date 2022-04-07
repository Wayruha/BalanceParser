package com.example.binanceparser.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class StatsChartBuilder {
    public JFreeChart buildDelayChart(List<Integer> items, int precision) {
        final ChartConfig chartConfig = new ChartConfig("Cloning delay", "Delay", "Occur");
        final LegendItem legend = new LegendItem("Cloning delay, ms");
        items = items.stream()
                .map(i -> Math.floorDiv(i, precision) * precision)
                .collect(Collectors.toList());
        return buildFrequencyChart(items, chartConfig, List.of(legend));
    }

    public JFreeChart buildPriceDeviationChart(List<Double> items) {
        final ChartConfig chartConfig = new ChartConfig("Price Deviation", "Deviation", "Occur");
        final LegendItem legend = new LegendItem("Order price deviation, %");
        items = items.stream()
                .map(i -> i * 100).collect(Collectors.toList());
        return buildFrequencyChart(items, chartConfig, List.of(legend));
    }

    public <T extends Number> JFreeChart buildIncomeDeviationChart(List<Double> items) {
        final ChartConfig chartConfig = new ChartConfig("Income Deviation", "Deviation", "Count");
        final LegendItem legend = new LegendItem("Position income deviation, %");
        items = items.stream()
                .map(i -> i * 100).collect(Collectors.toList());
        return buildFrequencyChart(items, chartConfig, List.of(legend));
    }

    public <T extends Number> JFreeChart buildFrequencyChart(List<T> items, ChartConfig config, List<LegendItem> legendItems) {
        final XYSeriesCollection data = new XYSeriesCollection();
        final XYSeries series = new XYSeries(config.getTitle());
        final Map<T, Integer> dataset = toFrequenciesMap(items);
        dataset.forEach(series::addOrUpdate);
        data.addSeries(series);

        final JFreeChart chart = ChartFactory.createXYLineChart(config.getTitle(), config.getXAxisLabel(), config.getYAxisLabel(), data);
        legendItems.forEach(item -> applyLegend(chart, item));
        return chart;
    }

    private static <T extends Number> Map<T, Integer> toFrequenciesMap(List<T> items) {
        final Map<T, Integer> frequencyMap = new HashMap<>();
        items.forEach(item -> frequencyMap.merge(item, 1, Integer::sum));
        return frequencyMap;
    }

    private void applyLegend(JFreeChart chart, LegendItem... items) {
        chart.addLegend(new LegendTitle(() -> {
            LegendItemCollection collection = new LegendItemCollection();
            for (LegendItem item : items) {
                collection.add(item);
            }
            return collection;
        }));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private final class ChartConfig {
        private String title;
        private String xAxisLabel;
        private String yAxisLabel;
    }
}
